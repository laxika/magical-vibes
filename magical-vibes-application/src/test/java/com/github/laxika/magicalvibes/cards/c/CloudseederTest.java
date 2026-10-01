package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.w.WhipSpineDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cloudseeder.class, BlindPhantasm.class, WhipSpineDrake.class})
class CloudseederTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and paying blue creates a Cloud Sprite")
    void createsCloudSprite() {
        Permanent cloudseeder = addCreatureReady(player1, new Cloudseeder());
        harness.setHand(player1, List.of(new BlindPhantasm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(cloudseeder.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Blind Phantasm");

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
        addCreatureReady(player1, new Cloudseeder());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new Cloudseeder());
        harness.setHand(player1, List.of(new BlindPhantasm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Blind Phantasm");
    }

    @Test
    @DisplayName("Cannot activate while Cloudseeder is summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new Cloudseeder());
        harness.setHand(player1, List.of(new BlindPhantasm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Cloudseeder is tapped")
    void cannotActivateWhileTapped() {
        Permanent cloudseeder = addCreatureReady(player1, new Cloudseeder());
        cloudseeder.tap();
        harness.setHand(player1, List.of(new BlindPhantasm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cloud Sprite can block a creature with flying")
    void cloudSpriteCanBlockFlyingCreature() {
        Permanent token = createCloudSprite();
        addAttackingCreature(player2, new WhipSpineDrake());

        declareBlock(token, 0);

        assertThat(token.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloud Sprite cannot block a creature without flying")
    void cloudSpriteCannotBlockGroundCreature() {
        Permanent token = createCloudSprite();
        addAttackingCreature(player2, new BlindPhantasm());

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player1.getId()).indexOf(token), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    private Permanent createCloudSprite() {
        addCreatureReady(player1, new Cloudseeder());
        harness.setHand(player1, List.of(new BlindPhantasm()));
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
        Permanent attacker = addCreatureReady(player, card);
        attacker.setAttacking(true);
    }

    private void declareBlock(Permanent blocker, int attackerIndex) {
        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker), attackerIndex)));
    }
}
