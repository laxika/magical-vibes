package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrigidClachansHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Kithkin token when it enters the battlefield")
    void createsKithkinOnEnter() {
        harness.setHand(player1, List.of(new BrigidClachansHeart()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countKithkinTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforms into Brigid, Doun's Mind after paying green in the first main phase")
    void transformsToBackFaceAfterPayingGreen() {
        Permanent brigid = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brigid.isTransformed()).isTrue();
        assertThat(brigid.getCard().getName()).isEqualTo("Brigid, Doun's Mind");
    }

    @Test
    @DisplayName("Transforms back and creates a Kithkin token after paying white in the first main phase")
    void transformsToFrontFaceAfterPayingWhite() {
        Permanent brigid = addBackFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(brigid.isTransformed()).isFalse();
        assertThat(brigid.getCard().getName()).isEqualTo("Brigid, Clachan's Heart");
        assertThat(countKithkinTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Back face adds green mana for each other creature")
    void backFaceAddsGreenForOtherCreatures() {
        Permanent brigid = addBackFace(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.activateAbility(player1, indexOf(player1, brigid), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Back face adds white mana for each other creature")
    void backFaceAddsWhiteForOtherCreatures() {
        Permanent brigid = addBackFace(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.activateAbility(player1, indexOf(player1, brigid), 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
    }

    private Permanent addFrontFace(Player player) {
        BrigidClachansHeart card = new BrigidClachansHeart();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBackFace(Player player) {
        BrigidClachansHeart card = new BrigidClachansHeart();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
    }

    private long countKithkinTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(perm -> perm.getCard().isToken())
                .filter(perm -> perm.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .count();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
