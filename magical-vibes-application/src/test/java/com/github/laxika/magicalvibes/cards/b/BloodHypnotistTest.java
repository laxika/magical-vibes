package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodHypnotist.class, TravelingMinister.class})
class BloodHypnotistTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        addCreatureReady(player1, new TravelingMinister()).setAttacking(true);
        addCreatureReady(player2, new BloodHypnotist());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A sacrificed Blood token makes a target creature unable to block this turn")
    void sacrificedBloodTokenMakesTargetUnableToBlock() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        Permanent blood = addBloodToken(player1);

        sacrificeBloodToken(player1, blood, target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A non-Blood sacrifice does not trigger")
    void nonBloodSacrificeDoesNotTrigger() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        Permanent treasure = addSacrificeToken(player1, "Treasure", CardSubtype.TREASURE);

        sacrificeToken(player1, treasure, target);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent firstTarget = addCreatureReady(player2, new TravelingMinister());
        Permanent secondTarget = addCreatureReady(player2, new TravelingMinister());
        Permanent firstBlood = addBloodToken(player1);
        Permanent secondBlood = addBloodToken(player1);

        sacrificeBloodToken(player1, firstBlood, firstTarget);
        sacrificeToken(player1, secondBlood, secondTarget);

        assertThat(firstTarget.isCantBlockThisTurn()).isTrue();
        assertThat(secondTarget.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A non-Blood sacrifice does not consume the once-per-turn trigger")
    void nonBloodSacrificeLeavesBloodTriggerAvailable() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        Permanent treasure = addSacrificeToken(player1, "Treasure", CardSubtype.TREASURE);
        Permanent blood = addBloodToken(player1);

        sacrificeToken(player1, treasure, target);
        sacrificeBloodToken(player1, blood, target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Blood sacrifice does not trigger or consume your trigger")
    void opponentsBloodSacrificeDoesNotTrigger() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        Permanent opponentsBlood = addBloodToken(player2);
        Permanent ownBlood = addBloodToken(player1);

        sacrificeToken(player2, opponentsBlood, target);

        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        sacrificeBloodToken(player1, ownBlood, target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The trigger can target a creature you control")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent target = addCreatureReady(player1, new TravelingMinister());
        Permanent blood = addBloodToken(player1);

        sacrificeBloodToken(player1, blood, target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires and the trigger resets on the next turn")
    void restrictionExpiresAndTriggerResetsNextTurn() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent firstTarget = addCreatureReady(player2, new TravelingMinister());
        Permanent secondTarget = addCreatureReady(player2, new TravelingMinister());
        Permanent firstBlood = addBloodToken(player1);
        Permanent secondBlood = addBloodToken(player1);

        sacrificeBloodToken(player1, firstBlood, firstTarget);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(firstTarget.isCantBlockThisTurn()).isFalse();

        sacrificeBloodToken(player1, secondBlood, secondTarget);

        assertThat(secondTarget.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The triggered ability resolves after Blood Hypnotist leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent hypnotist = addCreatureReady(player1, new BloodHypnotist());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        Permanent blood = addBloodToken(player1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(hypnotist);
        gd.playerGraveyards.get(player1.getId()).add(hypnotist.getCard());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The targeted creature cannot actually be declared as a blocker")
    void targetedCreatureCannotBlock() {
        addCreatureReady(player1, new BloodHypnotist());
        addCreatureReady(player1, new TravelingMinister()).setAttacking(true);
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        Permanent blood = addBloodToken(player1);

        sacrificeBloodToken(player1, blood, target);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Blood permanent that is not a token does not trigger")
    void nontokenBloodDoesNotTrigger() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        Permanent blood = addBloodToken(player1);
        blood.getCard().setToken(false);

        sacrificeToken(player1, blood, target);

        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void sacrificeBloodToken(Player player, Permanent blood, Permanent target) {
        sacrificeToken(player, blood, target);
    }

    private void sacrificeToken(Player player, Permanent token, Permanent target) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        harness.activateAbility(player, battlefield.indexOf(token), null, null);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player, target.getId());
        }
        resolveAllTriggers();
    }

    private Permanent addBloodToken(Player player) {
        return addSacrificeToken(player, "Blood", CardSubtype.BLOOD);
    }

    private Permanent addSacrificeToken(Player player, String name, CardSubtype subtype) {
        Card tokenCard = new Card();
        tokenCard.setName(name);
        tokenCard.setType(CardType.ARTIFACT);
        tokenCard.setToken(true);
        tokenCard.setSubtypes(List.of(subtype));
        tokenCard.addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost()),
                "Sacrifice this token."
        ));
        return addCreatureReady(player, tokenCard);
    }
}
