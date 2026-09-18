package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZamrielSeraphOfSteel.class, GrizzlyBears.class, LeoninScimitar.class})
class ZamrielSeraphOfSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creatures you control have indestructible during your turn")
    void equippedCreaturesYouControlHaveIndestructibleDuringYourTurn() {
        Permanent zamriel = addCreatureReady(player1, new ZamrielSeraphOfSteel());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent unequippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(equippedCreature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unequippedCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, zamriel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The indestructible grant does not apply during an opponent's turn")
    void grantDoesNotApplyDuringOpponentsTurn() {
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(equippedCreature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The grant affects only equipped creatures controlled by Zamriel's controller")
    void grantIsLimitedToEquippedCreaturesYouControl() {
        addCreatureReady(player1, new ZamrielSeraphOfSteel());
        Permanent ownEquippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentEquippedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        ownEquipment.setAttachedTo(ownEquippedCreature.getId());
        opponentEquipment.setAttachedTo(opponentEquippedCreature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, ownEquippedCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentEquippedCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
