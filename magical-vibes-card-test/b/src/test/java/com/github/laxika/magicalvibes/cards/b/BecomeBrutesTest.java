package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RedcapThief;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BecomeBrutes.class, GrizzlyBears.class, RedcapThief.class})
class BecomeBrutesTest extends BaseCardTest {

    @Test
    void omittingOptionalTargetCreatesOnlyOneRole() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castBecomeBrutes(List.of(target.getId()));

        assertThat(findPermanents(player1, "Monster"))
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void createsRoleForEachChosenTargetGroup() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        castBecomeBrutes(List.of(first.getId(), second.getId()));

        assertThat(findPermanents(player1, "Monster"))
                .extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    void hasteExpiresButMonsterRoleRemains() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedcapThief());

        castBecomeBrutes(List.of(target.getId()));

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(findPermanents(player1, "Monster")).hasSize(1);
    }

    @Test
    void newRoleReplacesAnOlderRoleControlledByTheCaster() {
        Permanent target = addCreatureReady(player1, new RedcapThief());
        castBecomeBrutes(List.of(target.getId()));
        UUID oldRoleId = findPermanent(player1, "Monster").getId();

        castBecomeBrutes(List.of(target.getId()));

        assertThat(findPermanents(player1, "Monster")).singleElement()
                .satisfies(role -> {
                    assertThat(role.getId()).isNotEqualTo(oldRoleId);
                    assertThat(role.getAttachedTo()).isEqualTo(target.getId());
                });
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void rolesControlledByDifferentPlayersCanEnchantTheSameCreature() {
        Permanent target = addCreatureReady(player1, new RedcapThief());
        castBecomeBrutes(List.of(target.getId()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BecomeBrutes()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player2, 0, List.of(target.getId()));

        assertThat(findPermanents(player1, "Monster")).hasSize(1);
        assertThat(findPermanents(player2, "Monster")).singleElement()
                .extracting(Permanent::getAttachedTo).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void resolvesOnlyForTheTargetStillOnTheBattlefield(boolean removeFirst) {
        Permanent first = addCreatureReady(player1, new RedcapThief());
        Permanent second = addCreatureReady(player2, new RedcapThief());
        harness.setHand(player1, List.of(new BecomeBrutes()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        Permanent survivor = removeFirst ? second : first;
        gd.playerBattlefields.get(removeFirst ? player1.getId() : player2.getId())
                .remove(removeFirst ? first : second);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monster")).singleElement()
                .extracting(Permanent::getAttachedTo).isEqualTo(survivor.getId());
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
    }

    @Test
    void createsNoRolesWhenAllTargetsLeaveBeforeResolution() {
        Permanent target = addCreatureReady(player1, new RedcapThief());
        harness.setHand(player1, List.of(new BecomeBrutes()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monster")).isEmpty();
        harness.assertInGraveyard(player1, "Become Brutes");
    }

    @Test
    void rejectsChoosingTheSameCreatureTwice() {
        Permanent target = addCreatureReady(player1, new RedcapThief());
        harness.setHand(player1, List.of(new BecomeBrutes()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresAtLeastOneTarget() {
        harness.setHand(player1, List.of(new BecomeBrutes()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.<UUID>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBecomeBrutes(List<UUID> targets) {
        harness.setHand(player1, List.of(new BecomeBrutes()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, targets);
    }
}
