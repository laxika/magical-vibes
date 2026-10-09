package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImperiousPerfect;
import com.github.laxika.magicalvibes.cards.s.SilvergillAdept;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CribSwap.class, GrizzlyBears.class, Forest.class, ImperiousPerfect.class,
        SilvergillAdept.class, WoodlandChangeling.class})
class CribSwapTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature and gives its controller a 1/1 colorless Shapeshifter with changeling")
    void exilesCreatureAndCreatesTokenForController() {
        GrizzlyBears target = new GrizzlyBears();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, target).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Target creature exiled (not to graveyard)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);

        // Its controller (player2) gets a 1/1 colorless Shapeshifter token with changeling
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shapeshifter")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == null
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.SHAPESHIFTER)
                        && p.getCard().getKeywords().contains(Keyword.CHANGELING));
    }

    @Test
    @DisplayName("Can target own creature — controller gets the Shapeshifter token")
    void canExileOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        // Controller (player1) gets the token
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shapeshifter")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution — no token created")
    void fizzlesWhenTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotOnBattlefield(player2, "Shapeshifter");
    }

    @Test
    @DisplayName("The Shapeshifter token benefits from an Elf lord through changeling")
    void tokenIsAnElfForContinuousEffects() {
        harness.addToBattlefield(player2, new ImperiousPerfect());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling()).getId();
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Shapeshifter");
    }

    @Test
    @DisplayName("Exiling a creature token still gives its controller a replacement token")
    void canExileTheCreatedToken() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling()).getId();
        harness.setHand(player1, List.of(new CribSwap(), new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveInstant(player1, 0, targetId);
        Permanent originalToken = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();

        harness.castAndResolveInstant(player1, 0, originalToken.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent replacement = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(replacement.getId()).isNotEqualTo(originalToken.getId());
        assertThat(replacement.getCard().isToken()).isTrue();
        assertThat(replacement.getCard().getName()).isEqualTo("Shapeshifter");
    }

    @Test
    @DisplayName("The controller at resolution receives the token, while the owner receives the exiled card")
    void usesControllerAtResolution() {
        WoodlandChangeling creature = new WoodlandChangeling();
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        gd.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                java.util.UUID.randomUUID(), null, null, player1.getId(),
                new com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect(
                        com.github.laxika.magicalvibes.model.effect.ControlDuration.PERMANENT),
                target.getId(), null, null, com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT, 0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        harness.assertOnBattlefield(player1, "Shapeshifter");
        harness.assertNotOnBattlefield(player2, "Shapeshifter");
        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("Crib Swap can be revealed as a Merfolk card for Silvergill Adept")
    void changelingAppliesInHand() {
        CribSwap cribSwap = new CribSwap();
        harness.setHand(player1, List.of(new SilvergillAdept(), cribSwap));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silvergill Adept");
        assertThat(gd.playerHands.get(player1.getId())).contains(cribSwap).hasSize(2);
    }
}
