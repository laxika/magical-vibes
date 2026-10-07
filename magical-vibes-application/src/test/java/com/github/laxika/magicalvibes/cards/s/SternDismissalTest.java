package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.cards.r.RumblingSentry;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SternDismissal.class, RumblingSentry.class, OmenOfTheSea.class, TravelersAmulet.class,
        NyxbornCourser.class})
class SternDismissalTest extends BaseCardTest {

    @Test
    @DisplayName("Stern Dismissal returns an opponent's creature to its owner's hand")
    void returnsOpponentCreature() {
        Permanent target = addCreatureReady(player2, new RumblingSentry());
        castSternDismissal(target);

        harness.assertInHand(player2, "Rumbling Sentry");
        harness.assertNotOnBattlefield(player2, "Rumbling Sentry");
    }

    @Test
    @DisplayName("Stern Dismissal returns an opponent's enchantment to its owner's hand")
    void returnsOpponentEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OmenOfTheSea());
        castSternDismissal(target);

        harness.assertInHand(player2, "Omen of the Sea");
        harness.assertNotOnBattlefield(player2, "Omen of the Sea");
    }

    @Test
    @DisplayName("Stern Dismissal cannot target a creature controlled by its caster")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new RumblingSentry());
        addCreatureReady(player2, new RumblingSentry());
        prepareSternDismissal();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment an opponent controls");
    }

    @Test
    @DisplayName("Stern Dismissal cannot target an opponent's artifact")
    void cannotTargetOpponentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new TravelersAmulet());
        addCreatureReady(player2, new RumblingSentry());
        prepareSternDismissal();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment an opponent controls");
    }

    @Test
    @DisplayName("Stern Dismissal can return an enchantment creature")
    void returnsOpponentEnchantmentCreature() {
        Permanent target = addCreatureReady(player2, new NyxbornCourser());
        castSternDismissal(target);

        harness.assertInHand(player2, "Nyxborn Courser");
        harness.assertNotOnBattlefield(player2, "Nyxborn Courser");
    }

    @Test
    @DisplayName("Stern Dismissal cannot target its caster's enchantment")
    void cannotTargetOwnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OmenOfTheSea());
        addCreatureReady(player2, new RumblingSentry());
        prepareSternDismissal();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment an opponent controls");
    }

    @Test
    @DisplayName("An opponent-controlled creature owned by the caster returns to the caster's hand")
    void returnsStolenCreatureToOwner() {
        Permanent target = addCreatureReady(player2, new RumblingSentry());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        castSternDismissal(target);

        harness.assertInHand(player1, "Rumbling Sentry");
        harness.assertNotInHand(player2, "Rumbling Sentry");
        harness.assertNotOnBattlefield(player2, "Rumbling Sentry");
    }

    @Test
    @DisplayName("Stern Dismissal does not return a target that comes under its caster's control")
    void targetBecomesControlledByCaster() {
        Permanent target = addCreatureReady(player2, new RumblingSentry());
        prepareSternDismissal();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rumbling Sentry");
        harness.assertNotInHand(player1, "Rumbling Sentry");
        harness.assertNotInHand(player2, "Rumbling Sentry");
        harness.assertInGraveyard(player1, "Stern Dismissal");
    }

    @Test
    @DisplayName("Stern Dismissal does not return a target that has left the battlefield")
    void targetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new RumblingSentry());
        prepareSternDismissal();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rumbling Sentry");
        harness.assertNotInHand(player2, "Rumbling Sentry");
        harness.assertInGraveyard(player1, "Stern Dismissal");
    }

    private void castSternDismissal(Permanent target) {
        prepareSternDismissal();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareSternDismissal() {
        harness.setHand(player1, List.of(new SternDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
    }

}
