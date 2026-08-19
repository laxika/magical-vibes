package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiaIvorBaneOfBladeholdTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents a target creature's combat damage to a player and creates one Mite per damage")
    void preventsCombatDamageAndCreatesMites() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent attacker = addCreatureReady(player1, creatureWithPower(4));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();

        List<Permanent> mites = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(mites).hasSize(4);
        assertThat(mites).allSatisfy(mite -> {
            assertThat(mite.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(mite.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(mite.getCard().getSubtypes()).contains(com.github.laxika.magicalvibes.model.CardSubtype.PHYREXIAN,
                    com.github.laxika.magicalvibes.model.CardSubtype.MITE);
            assertThat(mite.hasKeyword(Keyword.TOXIC)).isTrue();
            assertThat(bls.canBlock(gd, mite)).isFalse();
        });
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt to a blocking creature")
    void onlyPreventsDamageToPlayers() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent attacker = addCreatureReady(player1, creatureWithPower(4));
        Permanent blocker = addCreatureReady(player2, creatureWithPower(4));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().isToken())).isTrue();
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new RiaIvorBaneOfBladehold());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card creatureWithPower(int power) {
        Card card = new GrizzlyBears();
        card.setPower(power);
        return card;
    }
}
