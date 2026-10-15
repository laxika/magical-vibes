package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurRootcaster;
import com.github.laxika.magicalvibes.cards.e.Envelop;
import com.github.laxika.magicalvibes.cards.g.GoretuskFirebeast;
import com.github.laxika.magicalvibes.cards.k.KraulStinger;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.p.PhantomNomad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CentaurRootcaster.class, Envelop.class, GoretuskFirebeast.class, KraulStinger.class, Mortivore.class, PhantomNomad.class, SelflessExorcist.class})
class SelflessExorcistTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and it deals its power as damage to Selfless Exorcist")
    void exilesCreatureAndDealsItsPowerToSource() {
        Permanent exorcist = addReadyExorcist();
        Card rootcaster = new CentaurRootcaster();
        harness.setGraveyard(player2, List.of(rootcaster));

        harness.activateAbility(player1, 0, null, rootcaster.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Centaur Rootcaster");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(rootcaster.getId());
        assertThat(exorcist.isTapped()).isTrue();
        assertThat(exorcist.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The exiled creature card's deathtouch applies to its damage")
    void exiledCreatureCardKeepsDeathtouch() {
        Permanent exorcist = addReadyExorcist();
        Card stinger = new KraulStinger();
        harness.setGraveyard(player2, List.of(stinger));

        harness.activateAbility(player1, 0, null, stinger.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(exorcist);
    }

    @Test
    @DisplayName("Rejects a noncreature card as the graveyard target")
    void rejectsNoncreatureTarget() {
        Permanent exorcist = addReadyExorcist();
        Card envelop = new Envelop();
        harness.setGraveyard(player2, List.of(envelop));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, envelop.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(exorcist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does nothing if the targeted card leaves the graveyard before resolution")
    void targetLeavingGraveyardPreventsExileAndDamage() {
        Permanent exorcist = addReadyExorcist();
        Card rootcaster = new CentaurRootcaster();
        harness.setGraveyard(player2, List.of(rootcaster));

        harness.activateAbility(player1, 0, null, rootcaster.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).doesNotContain(rootcaster.getId());
        assertThat(exorcist.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target a creature card in its controller's graveyard")
    void targetsCreatureInControllersGraveyard() {
        Permanent exorcist = addReadyExorcist();
        Card rootcaster = new CentaurRootcaster();
        harness.setGraveyard(player1, List.of(rootcaster));

        harness.activateAbility(player1, 0, null, rootcaster.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(rootcaster.getId());
        assertThat(exorcist.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles a zero-power creature card without dealing damage")
    void zeroPowerCreatureDealsNoDamage() {
        Permanent exorcist = addReadyExorcist();
        Card nomad = new PhantomNomad();
        harness.setGraveyard(player2, List.of(nomad));

        harness.activateAbility(player1, 0, null, nomad.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(nomad.getId());
        assertThat(exorcist.getMarkedDamage()).isZero();
    }

    private Permanent addReadyExorcist() {
        return addCreatureReady(player1, new SelflessExorcist());
    }

    @Test
    @DisplayName("Can target a creature card in its controller's graveyard")
    void targetsCreatureCardInItsControllersGraveyard() {
        Permanent exorcist = addCreatureReady(player1, new SelflessExorcist());
        Card firebeast = new GoretuskFirebeast();
        harness.setGraveyard(player1, List.of(firebeast));

        harness.activateAbility(player1, 0, null, firebeast.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Goretusk Firebeast");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(firebeast.getId());
        assertThat(exorcist.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles the target but deals no damage if Selfless Exorcist leaves before resolution")
    void sourceLeavingBattlefieldPreventsDamage() {
        Permanent exorcist = addCreatureReady(player1, new SelflessExorcist());
        Card firebeast = new GoretuskFirebeast();
        harness.setGraveyard(player2, List.of(firebeast));

        harness.activateAbility(player1, 0, null, firebeast.getId(), Zone.GRAVEYARD);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, exorcist));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Goretusk Firebeast");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(firebeast.getId());
    }

    @Test
    @DisplayName("Uses a creature card's characteristic-defined power after exiling it")
    void variablePowerIsEvaluatedInExile() {
        Permanent exorcist = addReadyExorcist();
        Card mortivore = new Mortivore();
        harness.setGraveyard(player2, List.of(mortivore, new CentaurRootcaster()));
        harness.setGraveyard(player1, List.of(new GoretuskFirebeast()));

        harness.activateAbility(player1, 0, null, mortivore.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Mortivore");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(mortivore.getId());
        assertThat(exorcist.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Selfless Exorcist");
    }

    @Test
    @DisplayName("A variable-power creature card can deal lethal damage")
    void variablePowerCreatureCanKillExorcist() {
        addReadyExorcist();
        Card mortivore = new Mortivore();
        harness.setGraveyard(player2, List.of(mortivore, new CentaurRootcaster(),
                new CentaurRootcaster(), new CentaurRootcaster(), new CentaurRootcaster()));

        harness.activateAbility(player1, 0, null, mortivore.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(mortivore.getId());
        harness.assertNotOnBattlefield(player1, "Selfless Exorcist");
        harness.assertInGraveyard(player1, "Selfless Exorcist");
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void summoningSicknessPreventsActivation() {
        Permanent exorcist = harness.addToBattlefieldAndReturn(player1, new SelflessExorcist());
        exorcist.setSummoningSick(true);
        Card rootcaster = new CentaurRootcaster();
        harness.setGraveyard(player2, List.of(rootcaster));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rootcaster.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(exorcist.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Centaur Rootcaster");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void tappedExorcistCannotActivateAgain() {
        Permanent exorcist = addReadyExorcist();
        Card rootcaster = new CentaurRootcaster();
        harness.setGraveyard(player2, List.of(rootcaster));

        harness.activateAbility(player1, 0, null, rootcaster.getId(), Zone.GRAVEYARD);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rootcaster.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(exorcist.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(exorcist.getMarkedDamage()).isEqualTo(2);
    }
}
