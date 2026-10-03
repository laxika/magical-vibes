package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoggartMob.class, BoggartSpriteChaser.class, BoggartShenanigans.class, Mulldrifter.class})
class BoggartMobTest extends BaseCardTest {

    private Permanent addReadyBoggartMob() {
        return addCreatureReady(player1, new BoggartMob());
    }

    private Permanent addReadyGoblin() {
        return addCreatureReady(player1, new BoggartSpriteChaser());
    }

    private Permanent addReadyNonGoblin() {
        return addCreatureReady(player1, new Mulldrifter());
    }

    private void runCombatDamage() {
        resolveCombat();
        resolveAllTriggers();
    }

    private void castBoggartMob() {
        harness.castFromHand(player1, new BoggartMob(), "{3}{B}");
        harness.passBothPriorities(); // resolve the creature spell -> champion ETB on the stack
    }

    private long goblinRogueTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Goblin Rogue"))
                .count();
    }

    @Test
    @DisplayName("Accepting the may ability creates a 1/1 black Goblin Rogue token")
    void goblinCombatDamageCreatesToken() {
        addReadyBoggartMob();
        Permanent goblin = addReadyGoblin();
        goblin.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Goblin Rogue"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("Declining the may ability creates no token")
    void decliningCreatesNoToken() {
        addReadyBoggartMob();
        Permanent goblin = addReadyGoblin();
        goblin.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(goblinRogueTokens()).isZero();
    }

    @Test
    @DisplayName("A non-Goblin dealing combat damage does not trigger Boggart Mob")
    void nonGoblinDoesNotTrigger() {
        addReadyBoggartMob();
        Permanent bears = addReadyNonGoblin();
        bears.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        // Player2 took combat damage but Boggart Mob's ability never fired.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(goblinRogueTokens()).isZero();
    }

    @Test
    @DisplayName("Boggart Mob triggers for itself when it deals combat damage")
    void triggersForItself() {
        Permanent mob = addReadyBoggartMob();
        mob.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(goblinRogueTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Champion ETB sacrifices Boggart Mob when no other Goblin is controlled")
    void championAutoSacrificesWithoutAnotherGoblin() {
        castBoggartMob();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boggart Mob");
        harness.assertInGraveyard(player1, "Boggart Mob");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Champion ETB offers only another Goblin controlled by Boggart Mob's controller")
    void championChoiceOnlyOffersAnotherControlledGoblin() {
        Permanent goblin = addReadyGoblin();
        Permanent nonGoblin = addReadyNonGoblin();
        Permanent opponentGoblin = addCreatureReady(player2, new BoggartSpriteChaser());

        castBoggartMob();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(goblin.getId())
                .doesNotContain(nonGoblin.getId(), opponentGoblin.getId());

        harness.handlePermanentChosen(player1, goblin.getId());

        harness.assertOnBattlefield(player1, "Boggart Mob");
        harness.assertNotOnBattlefield(player1, "Boggart Sprite-Chaser");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Boggart Sprite-Chaser"));
    }

    @Test
    @DisplayName("Champion returns the exiled Goblin when Boggart Mob leaves the battlefield")
    void championedGoblinReturnsWhenBoggartMobLeaves() {
        Permanent goblin = addReadyGoblin();
        castBoggartMob();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        Permanent mob = findPermanent(player1, "Boggart Mob");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mob));

        harness.assertNotOnBattlefield(player1, "Boggart Mob");
        harness.assertNotOnBattlefield(player1, "Boggart Sprite-Chaser");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Boggart Sprite-Chaser");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Boggart Sprite-Chaser"));
    }

    @Test
    @DisplayName("Champion can exile a noncreature Goblin permanent")
    void championCanExileGoblinEnchantment() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        castBoggartMob();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(goblin.getId());
        harness.handlePermanentChosen(player1, goblin.getId());

        harness.assertOnBattlefield(player1, "Boggart Mob");
        harness.assertNotOnBattlefield(player1, "Boggart Shenanigans");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Boggart Shenanigans"));
    }

    @Test
    @DisplayName("Champion can still exile a Goblin after Boggart Mob has left")
    void championResolvesAfterSourceLeaves() {
        Permanent goblin = addReadyGoblin();
        castBoggartMob();
        Permanent mob = findPermanent(player1, "Boggart Mob");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mob));
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(goblin.getId());
        harness.handlePermanentChosen(player1, goblin.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Boggart Sprite-Chaser");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Boggart Sprite-Chaser"));
    }

    @Test
    @DisplayName("Each Goblin dealing combat damage creates a separate optional token")
    void multipleGoblinsTriggerSeparately() {
        addReadyBoggartMob();
        addReadyGoblin().setAttacking(true);
        addReadyGoblin().setAttacking(true);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(goblinRogueTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Goblin dealing combat damage does not trigger Boggart Mob")
    void opponentGoblinDoesNotTrigger() {
        addReadyBoggartMob();
        addCreatureReady(player2, new BoggartSpriteChaser()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(goblinRogueTokens()).isZero();
    }
}
