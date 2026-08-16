package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoastalBulwarkTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 while its controller controls an Island")
    void getsBoostWithIsland() {
        harness.addToBattlefield(player1, new CoastalBulwark());
        harness.addToBattlefield(player1, new Island());

        Permanent bulwark = findPermanent(player1, "Coastal Bulwark");
        assertThat(gqs.getEffectivePower(gd, bulwark)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bulwark)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get the boost without an Island")
    void noBoostWithoutIsland() {
        harness.addToBattlefield(player1, new CoastalBulwark());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bulwark = findPermanent(player1, "Coastal Bulwark");
        assertThat(gqs.getEffectivePower(gd, bulwark)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bulwark)).isEqualTo(3);
    }

    @Test
    @DisplayName("Surveil 1 puts the top card into the graveyard when accepted")
    void surveilAccepted() {
        Permanent bulwark = addBulwarkReady(player1);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(bulwark.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining surveil 1 leaves the top card on the library")
    void surveilDeclined() {
        Permanent bulwark = addBulwarkReady(player1);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(bulwark.isTapped()).isTrue();
    }

    private Permanent addBulwarkReady(Player player) {
        Permanent perm = new Permanent(new CoastalBulwark());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
