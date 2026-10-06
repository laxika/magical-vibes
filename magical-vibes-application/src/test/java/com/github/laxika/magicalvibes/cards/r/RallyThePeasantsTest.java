package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Dissipate;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RallyThePeasants.class, WalkingCorpse.class, Dissipate.class, Plains.class})
class RallyThePeasantsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(RallyThePeasants.class);
    }

    @Test
    @DisplayName("Resolving boosts all own creatures +2/+0")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(2);
                assertThat(p.getToughnessModifier()).isEqualTo(0);
                assertThat(p.getEffectivePower()).isEqualTo(4);
                assertThat(p.getEffectiveToughness()).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : p1Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(2);
            }
        }

        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
                assertThat(p.getToughnessModifier()).isEqualTo(0);
                assertThat(p.getEffectivePower()).isEqualTo(2);
                assertThat(p.getEffectiveToughness()).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("Goes to graveyard after normal cast resolves")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rally the Peasants");
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback boosts all own creatures +2/+0")
    void flashbackBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(2);
                assertThat(p.getEffectivePower()).isEqualTo(4);
            }
        }
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesSpellAfterResolving() {
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertNotInGraveyard(player1, "Rally the Peasants");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rally the Peasants"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack with flashback flag")
    void flashbackPutsOnStack() {
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(RallyThePeasants.class);
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Flashback pays flashback cost ({2}{R})")
    void flashbackPaysFlashbackCost() {
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback removes card from graveyard when cast")
    void flashbackRemovesFromGraveyard() {
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0);

        harness.assertNotInGraveyard(player1, "Rally the Peasants");
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotThoseEnteringLater() {
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(4);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void normalCastAndFlashbackBoostsAccumulateInTheSameTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(creature.getEffectivePower()).isEqualTo(6);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Rally the Peasants");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof RallyThePeasants);
    }

    @Test
    void flashbackRequiresRedManaAndDoesNotAcceptTheNormalWhiteCost() {
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Rally the Peasants");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void normalCastRequiresWhiteManaAndDoesNotAcceptTheFlashbackRedCost() {
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Rally the Peasants");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counteredFlashbackIsExiledWithoutBoostingCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFlashback(player1, 0);
        var spellId = gd.stack.getFirst().getTargetableId();
        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player2, 0, spellId);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Rally the Peasants");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof RallyThePeasants);
    }

    @Test
    void doesNotBoostNoncreaturePermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(land.getPowerModifier()).isZero();
        assertThat(land.getToughnessModifier()).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(4);
    }

    @Test
    void flashbackAcceptsGenericManaAndBoostLastsOnlyUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new RallyThePeasants()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }
}
