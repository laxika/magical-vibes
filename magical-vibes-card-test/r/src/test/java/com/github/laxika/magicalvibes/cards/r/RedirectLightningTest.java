package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedirectLightning.class, Boomerang.class, GrizzlyBears.class, CounselOfTheSoratami.class,
        ProdigalPyromancer.class, ConeOfFlame.class})
class RedirectLightningTest extends BaseCardTest {

    @Test
    void redirectsSpellAfterPayingLife() {
        UUID target1 = addTargetCreatures();
        UUID target2 = harness.getPermanentId(player2, "Grizzly Bears");
        Boomerang boomerang = castBoomerang(target1);
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstantWithLifeOrManaAdditionalCost(player2, 0, boomerang.getId(), true);
        GameData gameData = harness.getGameData();
        assertThat(gameData.getLife(player2.getId())).isEqualTo(15);
        assertThat(gameData.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, target2);
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId())).extracting(p -> p.getId()).contains(target1);
        assertThat(gameData.playerBattlefields.get(player2.getId())).extracting(p -> p.getId()).doesNotContain(target2);
    }

    @Test
    void redirectsSpellAfterPayingMana() {
        UUID target1 = addTargetCreatures();
        UUID target2 = harness.getPermanentId(player2, "Grizzly Bears");
        Boomerang boomerang = castBoomerang(target1);
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstantWithLifeOrManaAdditionalCost(player2, 0, boomerang.getId(), false);
        GameData gameData = harness.getGameData();
        assertThat(gameData.getLife(player2.getId())).isEqualTo(20);
        assertThat(gameData.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, target2);
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId())).extracting(p -> p.getId()).contains(target1);
        assertThat(gameData.playerBattlefields.get(player2.getId())).extracting(p -> p.getId()).doesNotContain(target2);
    }

    @Test
    void requiresSingleTargetSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithLifeOrManaAdditionalCost(
                player2, 0, counsel.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    void cannotTargetSpellWithMultipleTargets() {
        UUID creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        ConeOfFlame cone = new ConeOfFlame();
        harness.setHand(player1, List.of(cone));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, List.of(creature, player1.getId(), player2.getId()));
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithLifeOrManaAdditionalCost(
                player2, 0, cone.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Redirect Lightning");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void redirectsActivatedAbilityToItsController() {
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.activateAbility(player1, 0, null, player2.getId());
        UUID abilityId = gd.stack.getFirst().getTargetableId();
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstantWithLifeOrManaAdditionalCost(player2, 0, abilityId, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void leavesOriginalTargetUnchangedWhenThereIsNoOtherLegalTarget() {
        UUID target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        Boomerang boomerang = castBoomerang(target);
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstantWithLifeOrManaAdditionalCost(player2, 0, boomerang.getId(), true);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 15);
    }

    @Test
    void cannotPayLifeWithLessThanFiveLife() {
        Boomerang boomerang = castBoomerang(addTargetCreatures());
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.setLife(player2, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithLifeOrManaAdditionalCost(
                player2, 0, boomerang.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertLife(player2, 4);
        harness.assertInHand(player2, "Redirect Lightning");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void manaOptionRequiresTwoManaInAdditionToTheRedManaCost() {
        Boomerang boomerang = castBoomerang(addTargetCreatures());
        harness.setHand(player2, List.of(new RedirectLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithLifeOrManaAdditionalCost(
                player2, 0, boomerang.getId(), false))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Redirect Lightning");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
    }

    private UUID addTargetCreatures() {
        UUID target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new GrizzlyBears());
        return target;
    }

    private Boomerang castBoomerang(UUID targetId) {
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        return boomerang;
    }
}
