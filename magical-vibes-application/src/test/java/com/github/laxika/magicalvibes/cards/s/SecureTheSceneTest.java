package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecureTheScene.class, WalkingCorpse.class, Forest.class, ShortSword.class})
class SecureTheSceneTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent and gives its controller a Soldier token")
    void exilesNonlandPermanentAndCreatesSoldierForItsController() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        castSecureTheScene(player2, "Walking Corpse");

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Walking Corpse"));
        assertSoldierToken(player2);
    }

    @Test
    @DisplayName("Gives the caster a Soldier token when exiling their own permanent")
    void createsSoldierForCasterWhenExilingOwnPermanent() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        castSecureTheScene(player1, "Walking Corpse");

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        assertSoldierToken(player1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SecureTheScene()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Fizzles when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new SecureTheScene()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        var targetId = harness.getPermanentId(player2, "Walking Corpse");

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Walking Corpse"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    @DisplayName("Exiles a noncreature artifact and creates exactly one Soldier")
    void exilesArtifact() {
        harness.addToBattlefield(player2, new ShortSword());

        castSecureTheScene(player2, "Short Sword");

        harness.assertNotOnBattlefield(player2, "Short Sword");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Short Sword"));
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertSoldierToken(player2);
    }

    @Test
    @DisplayName("Uses the target's controller at resolution rather than its owner")
    void createsSoldierForNewController() {
        var target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new SecureTheScene()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Walking Corpse"));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertSoldierToken(player1);
    }

    @Test
    @DisplayName("Exiling a token still creates a replacement Soldier")
    void exilesTokenAndCreatesSoldier() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        castSecureTheScene(player2, "Walking Corpse");
        var originalTokenId = harness.getPermanentId(player2, "Soldier");

        castSecureTheScene(player2, "Soldier");

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .noneMatch(permanent -> permanent.getId().equals(originalTokenId));
        assertSoldierToken(player2);
    }

    private void castSecureTheScene(com.github.laxika.magicalvibes.model.Player targetController,
                                    String targetName) {
        harness.setHand(player1, List.of(new SecureTheScene()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(targetController, targetName));
    }

    private void assertSoldierToken(com.github.laxika.magicalvibes.model.Player player) {
        assertThat(gd.playerBattlefields.get(player.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Soldier")
                        && permanent.getCard().getColor() == CardColor.WHITE
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER));
    }
}
