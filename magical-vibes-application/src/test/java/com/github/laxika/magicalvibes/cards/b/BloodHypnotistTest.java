package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodHypnotist.class, GrizzlyBears.class})
class BloodHypnotistTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
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
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent blood = addBloodToken(player1);

        sacrificeBloodToken(player1, blood, target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A non-Blood sacrifice does not trigger")
    void nonBloodSacrificeDoesNotTrigger() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent treasure = addSacrificeToken(player1, "Treasure", CardSubtype.TREASURE);

        sacrificeToken(player1, treasure, target);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new BloodHypnotist());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstBlood = addBloodToken(player1);
        Permanent secondBlood = addBloodToken(player1);

        sacrificeBloodToken(player1, firstBlood, firstTarget);
        sacrificeToken(player1, secondBlood, secondTarget);

        assertThat(firstTarget.isCantBlockThisTurn()).isTrue();
        assertThat(secondTarget.isCantBlockThisTurn()).isFalse();
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
