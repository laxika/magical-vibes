package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathgorgeScavenger.class, ColossalDreadmaw.class, AncientBrontodon.class, LightningStrike.class})
class DeathgorgeScavengerTest extends BaseCardTest {

    @CardUsed({DeathgorgeScavenger.class, ColossalDreadmaw.class, AncientBrontodon.class, LightningStrike.class})
    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("ETB exiling a creature card gains 2 life")
        void etbExileCreatureGainsLife() {
            ColossalDreadmaw bears = new ColossalDreadmaw();
            harness.setGraveyard(player1, List.of(bears));

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            castDeathgorgeScavenger();
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            harness.passBothPriorities();

            // Accept may ability — the chosen graveyard target is exiled and the effect resolves
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null).isTrue();
            harness.handleMayAbilityChosen(player1, true);

            // Creature card exiled: gain 2 life
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Colossal Dreadmaw"))).isTrue();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        @DisplayName("ETB exiling a noncreature card gives +1/+1 until end of turn")
        void etbExileNoncreatureGivesBoost() {
            LightningStrike shock = new LightningStrike();
            harness.setGraveyard(player1, List.of(shock));

            castDeathgorgeScavenger();
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
            harness.passBothPriorities();

            // Accept may ability — the chosen target is exiled
            harness.handleMayAbilityChosen(player1, true);

            // Noncreature card exiled: source gets +1/+1
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Lightning Strike"))).isTrue();

            Permanent scavenger = findPermanent(player1, "Deathgorge Scavenger");
            assertThat(scavenger.getPowerModifier()).isEqualTo(1);
            assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        }

        @Test
        @DisplayName("Declining the may ability does not exile anything")
        void etbDeclineMayDoesNothing() {
            ColossalDreadmaw bears = new ColossalDreadmaw();
            harness.setGraveyard(player1, List.of(bears));

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            castDeathgorgeScavenger();
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            harness.passBothPriorities();

            // Decline the may ability
            harness.handleMayAbilityChosen(player1, false);

            // Card should still be in graveyard, no life gain
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.getOrDefault(player1.getId(), 20)).isEqualTo(lifeBefore);
        }
    }

    @CardUsed({DeathgorgeScavenger.class, ColossalDreadmaw.class, AncientBrontodon.class, LightningStrike.class})
    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking and exiling a creature card gains 2 life")
        void attackExileCreatureGainsLife() {
            ColossalDreadmaw bears = new ColossalDreadmaw();
            harness.setGraveyard(player1, List.of(bears));
            addCreatureReady(player1, new DeathgorgeScavenger());

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

            // Resolve MayEffect triggered ability on stack → may prompt
            harness.passBothPriorities();

            // Decide whether to exile the already chosen target
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null).isTrue();
            harness.handleMayAbilityChosen(player1, true);

            // Creature card exiled: gain 2 life
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Colossal Dreadmaw"))).isTrue();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        @DisplayName("Attacking and exiling a noncreature card gives +1/+1 until end of turn")
        void attackExileNoncreatureGivesBoost() {
            LightningStrike shock = new LightningStrike();
            harness.setGraveyard(player1, List.of(shock));
            Permanent scavenger = addCreatureReady(player1, new DeathgorgeScavenger());

            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            // Decide whether to exile the already chosen target
            harness.handleMayAbilityChosen(player1, true);

            // Noncreature card exiled: source gets +1/+1
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(scavenger.getPowerModifier()).isEqualTo(1);
            assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        }

        @Test
        @DisplayName("Declining the may ability on attack does not exile anything")
        void attackDeclineMayDoesNothing() {
            ColossalDreadmaw bears = new ColossalDreadmaw();
            harness.setGraveyard(player1, List.of(bears));
            addCreatureReady(player1, new DeathgorgeScavenger());

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            // Decline the may ability
            harness.handleMayAbilityChosen(player1, false);

            // Card should still be in graveyard, no life gain
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.getOrDefault(player1.getId(), 20)).isEqualTo(lifeBefore);
        }
    }

    @CardUsed({DeathgorgeScavenger.class, ColossalDreadmaw.class, AncientBrontodon.class, LightningStrike.class})
    @Nested
    @DisplayName("ETB with multiple graveyard targets")
    class ETBMultiTarget {

        @Test
        @DisplayName("Multiple graveyard cards prompt a choice; choosing a creature gains 2 life")
        void etbMultiTargetChooseCreatureGainsLife() {
            ColossalDreadmaw bears = new ColossalDreadmaw();
            AncientBrontodon giant = new AncientBrontodon();
            harness.setGraveyard(player1, List.of(bears, giant));

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            castDeathgorgeScavenger();
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null).isTrue();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Colossal Dreadmaw"))).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        @DisplayName("Multiple graveyard cards prompt a choice; choosing a noncreature gives +1/+1")
        void etbMultiTargetChooseNoncreatureGivesBoost() {
            ColossalDreadmaw bears = new ColossalDreadmaw();
            LightningStrike shock = new LightningStrike();
            harness.setGraveyard(player1, List.of(bears, shock));

            castDeathgorgeScavenger();
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
            harness.passBothPriorities();

            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Lightning Strike"))).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

            Permanent scavenger = findPermanent(player1, "Deathgorge Scavenger");
            assertThat(scavenger.getPowerModifier()).isEqualTo(1);
            assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        }
    }

    @CardUsed({DeathgorgeScavenger.class, ColossalDreadmaw.class, AncientBrontodon.class, LightningStrike.class})
    @Nested
    @DisplayName("Attack with multiple graveyard targets")
    class AttackMultiTarget {

        @Test
        @DisplayName("Attacking with multiple graveyard targets; choosing a creature gains 2 life")
        void attackMultiTargetChooseCreatureGainsLife() {
            ColossalDreadmaw bears = new ColossalDreadmaw();
            AncientBrontodon giant = new AncientBrontodon();
            harness.setGraveyard(player1, List.of(bears, giant));
            addCreatureReady(player1, new DeathgorgeScavenger());

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            harness.passBothPriorities(); // resolve MayEffect triggered ability → may prompt

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null).isTrue();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Colossal Dreadmaw"))).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        @DisplayName("Attacking with multiple graveyard targets; choosing a noncreature gives +1/+1")
        void attackMultiTargetChooseNoncreatureGivesBoost() {
            ColossalDreadmaw bears = new ColossalDreadmaw();
            LightningStrike shock = new LightningStrike();
            harness.setGraveyard(player1, List.of(bears, shock));
            Permanent scavenger = addCreatureReady(player1, new DeathgorgeScavenger());

            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Lightning Strike"))).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(scavenger.getPowerModifier()).isEqualTo(1);
            assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("ETB can exile a creature from the opponent's graveyard")
    void etbTargetsOpponentsGraveyard() {
        ColossalDreadmaw creature = new ColossalDreadmaw();
        harness.setGraveyard(player2, List.of(creature));

        castDeathgorgeScavenger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An ETB target removed before resolution gives no bonus")
    void etbTargetRemovedBeforeResolution() {
        ColossalDreadmaw creature = new ColossalDreadmaw();
        harness.setGraveyard(player1, List.of(creature));

        castDeathgorgeScavenger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        harness.assertLife(player1, 20);
        Permanent scavenger = findPermanent(player1, "Deathgorge Scavenger");
        assertThat(scavenger.getPowerModifier()).isZero();
        assertThat(scavenger.getToughnessModifier()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The creature-card life gain still resolves after the source leaves")
    void etbLifeGainWithoutSource() {
        ColossalDreadmaw creature = new ColossalDreadmaw();
        harness.setGraveyard(player2, List.of(creature));

        castDeathgorgeScavenger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        Permanent scavenger = findPermanent(player1, "Deathgorge Scavenger");
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, scavenger.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Deathgorge Scavenger");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .doesNotContain(creature);
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(creature.getId()));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The noncreature bonus expires at the end of the turn")
    void etbBonusExpires() {
        LightningStrike spell = new LightningStrike();
        harness.setGraveyard(player1, List.of(spell));

        castDeathgorgeScavenger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent scavenger = findPermanent(player1, "Deathgorge Scavenger");
        assertThat(scavenger.getPowerModifier()).isEqualTo(1);
        assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        harness.assertLife(player1, 20);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(scavenger.getPowerModifier()).isZero();
        assertThat(scavenger.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("ETB with empty graveyards offers no exile choice")
    void etbWithoutLegalTarget() {
        castDeathgorgeScavenger();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Attack target selection includes cards in both graveyards")
    void attackCanChooseOwnCardWithBothGraveyardsPopulated() {
        ColossalDreadmaw creature = new ColossalDreadmaw();
        LightningStrike spell = new LightningStrike();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(spell));
        addCreatureReady(player1, new DeathgorgeScavenger());

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(spell);
        harness.assertLife(player1, 22);
    }

    private void castDeathgorgeScavenger() {
        harness.setHand(player1, List.of(new DeathgorgeScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

}
