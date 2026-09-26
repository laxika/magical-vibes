package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Befoul;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TatsumasaTheDragonsFang.class, MothriderSamurai.class, Befoul.class})
class TatsumasaTheDragonsFangTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +5/+5")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new MothriderSamurai());
        Permanent tatsumasa = harness.addToBattlefieldAndReturn(player1, new TatsumasaTheDragonsFang());
        tatsumasa.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Activating the ability exiles Tatsumasa and creates a 5/5 flying Dragon Spirit token")
    void activationExilesSelfAndCreatesToken() {
        harness.addToBattlefieldAndReturn(player1, new TatsumasaTheDragonsFang());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tatsumasa, the Dragon's Fang");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tatsumasa, the Dragon's Fang"));

        Permanent token = findPermanent(player1, "Dragon Spirit");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON, CardSubtype.SPIRIT);
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip {3} attaches Tatsumasa to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent tatsumasa = harness.addToBattlefieldAndReturn(player1, new TatsumasaTheDragonsFang());
        Permanent creature = addCreatureReady(player1, new MothriderSamurai());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(tatsumasa.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Tatsumasa returns to the battlefield when the Dragon Spirit token dies")
    void tokenDeathReturnsTatsumasa() {
        harness.addToBattlefieldAndReturn(player1, new TatsumasaTheDragonsFang());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        killCreature(findPermanent(player1, "Dragon Spirit"));

        harness.assertOnBattlefield(player1, "Tatsumasa, the Dragon's Fang");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Tatsumasa, the Dragon's Fang"));
        harness.assertNotOnBattlefield(player1, "Dragon Spirit");
    }

    @Test
    @DisplayName("Tatsumasa returns under its owner's control when controlled by another player")
    void tokenDeathReturnsTatsumasaToOwner() {
        TatsumasaTheDragonsFang card = new TatsumasaTheDragonsFang();
        card.setOwnerId(player1.getId());
        Permanent tatsumasa = harness.addToBattlefieldAndReturn(player2, card);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        int tatsumasaIndex = gd.playerBattlefields.get(player2.getId()).indexOf(tatsumasa);
        harness.activateAbility(player2, tatsumasaIndex, null, null);
        harness.passBothPriorities();

        killCreature(findPermanent(player2, "Dragon Spirit"));

        harness.assertOnBattlefield(player1, "Tatsumasa, the Dragon's Fang");
        harness.assertNotOnBattlefield(player2, "Tatsumasa, the Dragon's Fang");
    }

    @Test
    @DisplayName("Tatsumasa stays in exile while the token is alive")
    void tatsumasaStaysExiledWhileTokenLives() {
        harness.addToBattlefieldAndReturn(player1, new TatsumasaTheDragonsFang());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tatsumasa, the Dragon's Fang");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tatsumasa, the Dragon's Fang"));
    }

    @Test
    @DisplayName("The returned Tatsumasa can be activated again")
    void returnedTatsumasaCanBeActivatedAgain() {
        harness.addToBattlefieldAndReturn(player1, new TatsumasaTheDragonsFang());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        killCreature(findPermanent(player1, "Dragon Spirit"));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tatsumasa, the Dragon's Fang"));
        harness.assertOnBattlefield(player1, "Dragon Spirit");
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Befoul()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Befoul — the token dies, its trigger goes on the stack
        harness.passBothPriorities(); // resolve the return trigger
    }
}
