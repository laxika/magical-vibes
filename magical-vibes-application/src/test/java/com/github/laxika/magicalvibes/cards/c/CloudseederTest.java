package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cloudseeder.class, GrizzlyBears.class, AirElemental.class})
class CloudseederTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and paying blue creates a Cloud Sprite")
    void createsCloudSprite() {
        Permanent cloudseeder = addReadyCloudseeder();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(cloudseeder.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Cloud Sprite");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FAERIE);
        assertThat(token.getCard().hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyCloudseeder();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cloud Sprite can block a creature with flying")
    void cloudSpriteCanBlockFlyingCreature() {
        Permanent token = createCloudSprite();
        addAttackingCreature(player2, new AirElemental());

        declareBlock(token, 0);

        assertThat(token.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloud Sprite cannot block a creature without flying")
    void cloudSpriteCannotBlockGroundCreature() {
        Permanent token = createCloudSprite();
        addAttackingCreature(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player1.getId()).indexOf(token), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    private Permanent addReadyCloudseeder() {
        Permanent cloudseeder = new Permanent(new Cloudseeder());
        cloudseeder.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cloudseeder);
        return cloudseeder;
    }

    private Permanent createCloudSprite() {
        addReadyCloudseeder();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void addAttackingCreature(Player player, Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
    }

    private void declareBlock(Permanent blocker, int attackerIndex) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker), attackerIndex)));
    }
}
