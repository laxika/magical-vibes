package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReflectionNet.class, GrizzlyBears.class, HillGiant.class})
class ReflectionNetTest extends BaseCardTest {

    @Test
    void exilesTargetCreatureUntilReflectionNetLeaves() {
        Permanent exiledCreature = addCreatureReady(player2, new HillGiant());
        Permanent net = castReflectionNet(exiledCreature);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(exiledCreature);
        assertThat(gd.findExiledCard(exiledCreature.getOriginalCard().getId()).sourcePermanentId())
                .isEqualTo(net.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, net));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == exiledCreature.getOriginalCard());
    }

    @Test
    void targetCreatureBecomesCopyOfExiledCreatureAndAbilityIsOneShot() {
        Permanent exiledCreature = addCreatureReady(player2, new HillGiant());
        castReflectionNet(exiledCreature);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(target.getCard().getPower()).isEqualTo(3);
        assertThat(target.getCard().getToughness()).isEqualTo(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can be activated only once");
    }

    @Test
    void abilityCannotTargetAnOpponentCreature() {
        Permanent exiledCreature = addCreatureReady(player2, new HillGiant());
        castReflectionNet(exiledCreature);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    void flashAllowsCastingDuringOpponentsUpkeep() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new ReflectionNet(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reflection Net");
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void sourceLeavingBeforeEnterTriggerResolvesDoesNotExileCreature() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ReflectionNet(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent net = findPermanent(player1, "Reflection Net");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, net));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
    }

    @Test
    void copyRemainsAfterNetLeavesAndOriginalCreatureReturns() {
        Permanent exiledCreature = addCreatureReady(player2, new HillGiant());
        Permanent net = castReflectionNet(exiledCreature);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, net));

        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == exiledCreature.getOriginalCard());
        assertThat(gd.findExiledCard(exiledCreature.getOriginalCard().getId())).isNull();
    }

    @Test
    void sourceLeavingInResponseToCopyAbilityPreventsCopy() {
        Permanent exiledCreature = addCreatureReady(player2, new HillGiant());
        Permanent net = castReflectionNet(exiledCreature);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, net));
        harness.passBothPriorities();

        assertThat(target.getCard()).isSameAs(target.getOriginalCard());
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void activatingWithoutExiledCreatureStillUsesOnlyActivation() {
        harness.addToBattlefield(player1, new ReflectionNet());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCard()).isSameAs(target.getOriginalCard());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can be activated only once");
    }

    @Test
    void copyAbilityCannotBeActivatedOutsideMainPhase() {
        Permanent exiledCreature = addCreatureReady(player2, new HillGiant());
        castReflectionNet(exiledCreature);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void copyAbilityCannotBeActivatedDuringOpponentsMainPhase() {
        Permanent exiledCreature = addCreatureReady(player2, new HillGiant());
        castReflectionNet(exiledCreature);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void copyAbilityCannotBeActivatedWithASpellOnTheStack() {
        Permanent exiledCreature = addCreatureReady(player2, new HillGiant());
        castReflectionNet(exiledCreature);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
    }

    private Permanent castReflectionNet(Permanent target) {
        Card netCard = new ReflectionNet();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, netCard, "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == netCard)
                .findFirst()
                .orElseThrow();
    }
}
