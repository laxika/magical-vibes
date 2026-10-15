package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AffectionateIndrik.class, DwynensElite.class, HerosDownfall.class})
class AffectionateIndrikTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability makes Affectionate Indrik fight an opponent's creature")
    void acceptingFightKillsSmallerCreature() {
        Permanent target = addCreature(player2);
        castIndrikAndResolveSpell();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Affectionate Indrik");
        harness.assertNotOnBattlefield(player2, "Dwynen's Elite");
    }

    @Test
    @DisplayName("Declining the ETB ability does not make Affectionate Indrik fight")
    void decliningFightLeavesBothCreaturesOnBattlefield() {
        Permanent target = addCreature(player2);
        castIndrikAndResolveSpell();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Affectionate Indrik");
        harness.assertOnBattlefield(player2, "Dwynen's Elite");
    }

    @Test
    @DisplayName("Both creatures deal fight damage even when the first damage is lethal")
    void equallySizedCreaturesBothDie() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AffectionateIndrik());
        castIndrikAndResolveSpell();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Affectionate Indrik");
        harness.assertInGraveyard(player2, "Affectionate Indrik");
        harness.assertNotOnBattlefield(player1, "Affectionate Indrik");
        harness.assertNotOnBattlefield(player2, "Affectionate Indrik");
    }

    @Test
    @DisplayName("Creatures you control cannot supply a target for the ETB ability")
    void noOpponentCreatureDoesNotPromptForFight() {
        Permanent ownCreature = addCreature(player1);
        castIndrikAndResolveSpell();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Affectionate Indrik");
    }

    @Test
    @DisplayName("Neither creature deals damage when Indrik leaves before the fight resolves")
    void missingSourceDoesNotDealFightDamage() {
        Permanent target = addCreature(player2);
        castIndrikAndResolveSpell();
        harness.handlePermanentChosen(player1, target.getId());
        destroyCreature(harness.getPermanentId(player1, "Affectionate Indrik"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Dwynen's Elite");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The fight ability does not resolve when its target leaves the battlefield")
    void missingTargetDoesNotDealFightDamage() {
        Permanent target = addCreature(player2);
        castIndrikAndResolveSpell();
        harness.handlePermanentChosen(player1, target.getId());
        destroyCreature(target.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A target that comes under your control is illegal when the fight resolves")
    void targetBecomingControlledByYouDoesNotFight() {
        Permanent target = addCreature(player2);
        castIndrikAndResolveSpell();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DwynensElite());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void destroyCreature(UUID targetId) {
        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void castIndrikAndResolveSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AffectionateIndrik(), "{5}{G}");
        harness.passBothPriorities();
    }
}
