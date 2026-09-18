package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainAssassination.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class ChainAssassinationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target creature without drawing when it is the only death")
    void destroysTargetWithoutDrawingForItsOwnDeath() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChainAssassination()));
        harness.setLibrary(player1, List.of(new Forest()));
        addNormalMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws a card when another creature died this turn")
    void drawsAfterAnotherCreatureDied() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(new Shock(), new ChainAssassination()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Draws when the legal target is indestructible and another creature died")
    void drawsWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(new ChainAssassination()));
        harness.setLibrary(player1, List.of(drawn));
        addNormalMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Freerunning casts for its alternate cost after Assassin combat damage")
    void freerunningCastsForAlternateCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        markAssassinCombatDamage();
        harness.setHand(player1, List.of(new ChainAssassination()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Freerunning requires qualifying combat damage")
    void freerunningRequiresQualifyingCombatDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChainAssassination()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ChainAssassination()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void markAssassinCombatDamage() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
    }
}
