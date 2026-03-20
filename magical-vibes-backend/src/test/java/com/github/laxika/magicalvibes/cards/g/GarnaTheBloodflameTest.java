package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DreamTwist;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GarnaTheBloodflameTest extends BaseCardTest {

    @Test
    @DisplayName("Has ETB effect that returns creature cards from graveyard put there from anywhere this turn")
    void hasCorrectETBEffect() {
        GarnaTheBloodflame card = new GarnaTheBloodflame();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        ReturnCardFromGraveyardEffect etbEffect =
                (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etbEffect.returnAll()).isTrue();
        assertThat(etbEffect.fromAnywhereThisTurn()).isTrue();
        assertThat(etbEffect.thisTurnOnly()).isFalse();
    }

    @Test
    @DisplayName("Has static haste grant for other creatures you control")
    void hasStaticHasteForOwnCreatures() {
        GarnaTheBloodflame card = new GarnaTheBloodflame();

        GrantKeywordEffect hasteEffect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .findFirst().orElseThrow();
        assertThat(hasteEffect.keyword()).isEqualTo(Keyword.HASTE);
        assertThat(hasteEffect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
    }

    @Test
    @DisplayName("ETB returns creature that died this turn from the battlefield")
    void etbReturnsCreatureThatDiedThisTurn() {
        Card creature = new GrizzlyBears();

        harness.addToBattlefield(player1, creature);
        harness.addToBattlefield(player1, new GrizzlyBears()); // a second one to shock

        UUID targetCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();

        // The killed creature should be in the graveyard now
        Card diedCreature = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Cast Garna
        harness.setHand(player1, List.of(new GarnaTheBloodflame()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(diedCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(diedCreature.getId()));
    }

    @Test
    @DisplayName("ETB returns creature card that was milled this turn (from anywhere, not just battlefield)")
    void etbReturnsMilledCreatureCard() {
        // Put a creature card on top of the library so it gets milled
        Card creatureInLibrary = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, creatureInLibrary);

        // Self-mill with Dream Twist
        harness.setHand(player1, List.of(new DreamTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Verify the creature was milled into graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureInLibrary.getId()));

        // Cast Garna
        harness.setHand(player1, List.of(new GarnaTheBloodflame()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // The milled creature card should be returned to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureInLibrary.getId()));
    }

    @Test
    @DisplayName("ETB does not return creature cards already in graveyard before this turn")
    void etbDoesNotReturnCreaturesFromPreviousTurn() {
        Card oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));

        // Cast Garna
        harness.setHand(player1, List.of(new GarnaTheBloodflame()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // Old creature should still be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(oldCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(oldCreature.getId()));
    }

    @Test
    @DisplayName("ETB does not return non-creature cards put into graveyard this turn")
    void etbDoesNotReturnNonCreatureCards() {
        // Put a non-creature card on top of the library so it gets milled
        Card nonCreature = new Shock();
        gd.playerDecks.get(player1.getId()).add(0, nonCreature);

        // Self-mill with Dream Twist to put the non-creature into graveyard
        harness.setHand(player1, List.of(new DreamTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Cast Garna
        harness.setHand(player1, List.of(new GarnaTheBloodflame()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // Shock should remain in graveyard (not a creature card)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(nonCreature.getId()));
    }

    @Test
    @DisplayName("Does not return creature cards put into an opponent's graveyard this turn")
    void doesNotReturnOpponentsCreatures() {
        Card opponentsCreature = new GrizzlyBears();

        harness.addToBattlefield(player2, opponentsCreature);

        UUID targetCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();

        // Cast Garna
        harness.setHand(player1, List.of(new GarnaTheBloodflame()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // Opponent's creature should still be in opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(opponentsCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(opponentsCreature.getId()));
    }

    @Test
    @DisplayName("Other creatures you control have haste while Garna is on the battlefield")
    void otherCreaturesHaveHaste() {
        harness.addToBattlefield(player1, new GarnaTheBloodflame());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Garna does not grant haste to itself (OWN_CREATURES scope)")
    void garnaDoesNotGrantHasteToItself() {
        harness.addToBattlefield(player1, new GarnaTheBloodflame());

        Permanent garna = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Garna, the Bloodflame"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, garna, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is lost when Garna leaves the battlefield")
    void hasteLostWhenGarnaLeaves() {
        GarnaTheBloodflame garnaCard = new GarnaTheBloodflame();
        harness.addToBattlefield(player1, garnaCard);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        // Remove Garna via Shock
        UUID garnaPermId = harness.getPermanentId(player1, "Garna, the Bloodflame");
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        // Garna is 3/3, needs two Shocks to kill
        harness.castInstant(player1, 0, garnaPermId);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, garnaPermId);
        harness.passBothPriorities();

        // Bears should no longer have haste
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }
}
