package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurRootcaster;
import com.github.laxika.magicalvibes.cards.e.Envelop;
import com.github.laxika.magicalvibes.cards.k.KraulStinger;
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

@CardUsed({SelflessExorcist.class, CentaurRootcaster.class, KraulStinger.class, Envelop.class, PhantomNomad.class})
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
}
