package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SculptorOfWinter;
import com.github.laxika.magicalvibes.cards.b.BrokenWings;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvenBow.class, SculptorOfWinter.class, BrokenWings.class})
class ElvenBowTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the ETB cost creates and equips an Elf Warrior")
    void payingEtbCostCreatesAndEquipsElfWarrior() {
        Permanent bow = castBowWithMana(3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent elfWarrior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(bow.getAttachedTo()).isEqualTo(elfWarrior.getId());
        assertThat(gqs.getEffectivePower(gd, elfWarrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elfWarrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elfWarrior, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB cost creates no Elf Warrior")
    void decliningEtbCostCreatesNoElfWarrior() {
        Permanent bow = castBowWithMana(3);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
        assertThat(bow.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {3} attaches Elven Bow to a creature you control")
    void equipAttachesBow() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new ElvenBow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    private Permanent castBowWithMana(int colorless) {
        harness.setHand(player1, List.of(new ElvenBow()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gqs.findPermanentById(gd, harness.getPermanentId(player1, "Elven Bow"));
    }

    @Test
    @DisplayName("The ETB payment spends exactly two mana and creates exactly one token")
    void etbPaymentSpendsExactlyTwoMana() {
        castBowWithMana(2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("The ETB attaches only the Bow that triggered, leaving other Bows alone")
    void etbAttachesOnlyItsSource() {
        Permanent otherBow = harness.addToBattlefieldAndReturn(player1, new ElvenBow());
        Permanent opposingBow = harness.addToBattlefieldAndReturn(player2, new ElvenBow());
        harness.setHand(player1, List.of(new ElvenBow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent enteringBow = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ElvenBow)
                .filter(permanent -> !permanent.getId().equals(otherBow.getId()))
                .findFirst().orElseThrow();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(enteringBow.getAttachedTo()).isEqualTo(token.getId());
        assertThat(otherBow.getAttachedTo()).isNull();
        assertThat(opposingBow.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("The token is still created if Elven Bow leaves before its ETB resolves")
    void tokenIsCreatedAfterBowIsDestroyed() {
        harness.setHand(player1, List.of(new ElvenBow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        UUID bowId = harness.getPermanentId(player1, "Elven Bow");
        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, bowId);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Elven Bow");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Moving Elven Bow removes its bonuses from the previous creature")
    void movingBowRemovesPreviousBonuses() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new ElvenBow());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isTrue();
    }
}
