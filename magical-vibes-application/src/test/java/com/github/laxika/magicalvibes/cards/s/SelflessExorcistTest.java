package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KraulStinger;
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

@CardUsed({SelflessExorcist.class, GrizzlyBears.class, KraulStinger.class, Cancel.class})
class SelflessExorcistTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and it deals its power as damage to Selfless Exorcist")
    void exilesCreatureAndDealsItsPowerToSource() {
        Permanent exorcist = addReadyExorcist();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(bears.getId());
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
        Card cancel = new Cancel();
        harness.addToBattlefield(player1, new SelflessExorcist());
        harness.setGraveyard(player2, List.of(cancel));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cancel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing if the targeted card leaves the graveyard before resolution")
    void targetLeavingGraveyardPreventsExileAndDamage() {
        Permanent exorcist = addReadyExorcist();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(exorcist.getMarkedDamage()).isZero();
    }

    private Permanent addReadyExorcist() {
        Permanent exorcist = harness.addToBattlefieldAndReturn(player1, new SelflessExorcist());
        exorcist.setSummoningSick(false);
        return exorcist;
    }
}
