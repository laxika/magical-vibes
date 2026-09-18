package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoretuskFirebeast;
import com.github.laxika.magicalvibes.cards.k.KraulStinger;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
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

@CardUsed({SelflessExorcist.class, GoretuskFirebeast.class, KraulStinger.class, MentalNote.class})
class SelflessExorcistTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and it deals its power as damage to Selfless Exorcist")
    void exilesCreatureAndDealsItsPowerToSource() {
        Permanent exorcist = addCreatureReady(player1, new SelflessExorcist());
        Card firebeast = new GoretuskFirebeast();
        harness.setGraveyard(player2, List.of(firebeast));

        harness.activateAbility(player1, 0, null, firebeast.getId(), Zone.GRAVEYARD);
        assertThat(exorcist.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Goretusk Firebeast");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(firebeast.getId());
        assertThat(exorcist.getMarkedDamage()).isEqualTo(2);
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
    @DisplayName("The exiled creature card's deathtouch applies to its damage")
    void exiledCreatureCardKeepsDeathtouch() {
        Permanent exorcist = addCreatureReady(player1, new SelflessExorcist());
        Card stinger = new KraulStinger();
        harness.setGraveyard(player2, List.of(stinger));

        harness.activateAbility(player1, 0, null, stinger.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(exorcist);
    }

    @Test
    @DisplayName("Rejects a noncreature card as the graveyard target")
    void rejectsNoncreatureTarget() {
        Card mentalNote = new MentalNote();
        harness.addToBattlefield(player1, new SelflessExorcist());
        harness.setGraveyard(player2, List.of(mentalNote));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mentalNote.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing if the targeted card leaves the graveyard before resolution")
    void targetLeavingGraveyardPreventsExileAndDamage() {
        Permanent exorcist = addCreatureReady(player1, new SelflessExorcist());
        Card firebeast = new GoretuskFirebeast();
        harness.setGraveyard(player2, List.of(firebeast));

        harness.activateAbility(player1, 0, null, firebeast.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(exorcist.getMarkedDamage()).isZero();
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
}
