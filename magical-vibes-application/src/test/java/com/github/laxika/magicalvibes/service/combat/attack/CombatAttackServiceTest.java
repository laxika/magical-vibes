package com.github.laxika.magicalvibes.service.combat.attack;

import com.github.laxika.magicalvibes.cards.a.AngelicArbiter;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.b.Brainwash;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.d.DuelingGrounds;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.e.EkunduCyclops;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FormOfTheDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JackalFamiliar;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.m.MagneticWeb;
import com.github.laxika.magicalvibes.cards.n.NornsAnnex;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.cards.o.OrcishConscripts;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SightlessBrawler;
import com.github.laxika.magicalvibes.cards.t.TroveOfTemptation;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.cards.w.WindbornMuse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Spec for {@link CombatAttackService}, the declare-attackers step itself: which creatures the
 * active player is offered, which of them are flagged as forced, and the declaration-time
 * validation that no single creature's legality can express — the group restrictions ("can't
 * attack alone", "can only attack alone", Okk, Orcish Conscripts), satisfying as many "attacks if
 * able" requirements as possible (CR 508.1d), and the summed cost of a whole declaration.
 *
 * <p>Per-creature legality is not retested here; that is {@link AttackLegalityServiceTest}'s spec,
 * and band validation is {@code BandingMechanicTest}'s. Cards appear only as the cheapest way to
 * reach a rule.
 */
class CombatAttackServiceTest extends BaseCardTest {

    private CombatAttackService service() {
        return harness.getCombatAttackService();
    }

    private int index(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    /** Puts the game in the state {@code declareAttackers} expects: player1 active, declaration open. */
    private void enterDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    /**
     * Submits a declaration through the sub-service rather than {@code gs.declareAttackers}: with
     * nothing to block, the full flow runs combat to completion and clears the very attacking state
     * these tests assert on.
     */
    private CombatResult declare(List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        CombatResult[] result = new CombatResult[1];
        harness.inMutationScope(() ->
                result[0] = service().declareAttackers(gd, player1, attackerIndices, attackTargets));
        return result[0];
    }

    private CombatResult declare(List<Integer> attackerIndices) {
        return declare(attackerIndices, null);
    }

    @Test
    @DisplayName("Reports the current maximum attacker count")
    void reportsMaximumAttackerCount() {
        assertThat(service().getMaximumAttackers(gd)).isEqualTo(Integer.MAX_VALUE);

        harness.addToBattlefield(player2, new DuelingGrounds());

        assertThat(service().getMaximumAttackers(gd)).isEqualTo(1);
    }

    @Nested
    @DisplayName("Attackers offered to the active player")
    class AttackableCreatures {

        @Test
        @DisplayName("Only creatures that pass the legality gate are offered")
        void onlyLegalAttackersAreOffered() {
            Permanent ready = addCreatureReady(player1, new GrizzlyBears());
            Permanent tapped = addCreatureReady(player1, new ScatheZombies());
            tapped.tap();
            harness.addToBattlefield(player1, new HillGiant());
            harness.addToBattlefield(player1, new Forest());

            assertThat(service().getAttackableCreatureIndices(gd, player1.getId()))
                    .containsExactly(index(ready));
        }

        @Test
        @DisplayName("A must-attack creature with no legal attack target is not offered")
        void mustAttackCreatureWithNoLegalTargetIsNotOffered() {
            addCreatureReady(player1, new Juggernaut());
            harness.addToBattlefield(player2, new FormOfTheDragon());

            assertThat(service().getAttackableCreatureIndices(gd, player1.getId())).isEmpty();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.inMutationScope(() -> service().handleDeclareAttackersStep(gd));

            assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("CR 508.1c: a lone \"can't attack alone\" creature leaves no legal attackers")
        void loneCantAttackAloneCreatureIsNotOffered() {
            Permanent familiar = addCreatureReady(player1, new JackalFamiliar());

            assertThat(service().getAttackableCreatureIndices(gd, player1.getId())).isEmpty();

            Permanent ally = addCreatureReady(player1, new GrizzlyBears());
            assertThat(service().getAttackableCreatureIndices(gd, player1.getId()))
                    .containsExactly(index(familiar), index(ally));
        }

        @Test
        @DisplayName("A player locked out of attacking is offered nothing, however ready their board")
        void lockedOutPlayerIsOfferedNothing() {
            addCreatureReady(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new AngelicArbiter());

            assertThat(service().getAttackableCreatureIndices(gd, player1.getId())).hasSize(1);

            gd.recordSpellCast(player1.getId(), new GrizzlyBears());

            assertThat(service().getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Identifies creatures that can only attack alone through an attached Aura")
        void identifiesCanOnlyAttackAloneRestrictionFromAura() {
            Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
            Permanent unrestricted = addCreatureReady(player1, new GrizzlyBears());
            Permanent aura = new Permanent(new Errantry());
            aura.setAttachedTo(enchanted.getId());
            gd.playerBattlefields.get(player1.getId()).add(aura);

            assertThat(service().canOnlyAttackAlone(gd, enchanted)).isTrue();
            assertThat(service().canOnlyAttackAlone(gd, unrestricted)).isFalse();
        }

        @Test
        @DisplayName("Identifies a can't-attack-alone restriction through an attached Aura")
        void identifiesCantAttackAloneRestrictionFromAura() {
            Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
            Permanent aura = new Permanent(new SightlessBrawler());
            aura.setAttachedTo(enchanted.getId());
            gd.playerBattlefields.get(player1.getId()).add(aura);

            assertThat(service().getAttackableCreatureIndices(gd, player1.getId()))
                    .isEmpty();

            Permanent unrestricted = addCreatureReady(player1, new GrizzlyBears());
            assertThat(service().getAttackableCreatureIndices(gd, player1.getId()))
                    .containsExactly(index(enchanted), index(unrestricted));
        }
    }

    @Nested
    @DisplayName("Creatures flagged as forced to attack")
    class MustAttackCreatures {

        @Test
        @DisplayName("Only creatures carrying a requirement are flagged")
        void onlyCreaturesWithARequirementAreFlagged() {
            addCreatureReady(player1, new GrizzlyBears());
            Permanent berserkers = addCreatureReady(player1, new BerserkersOfBloodRidge());

            List<Integer> attackable = service().getAttackableCreatureIndices(gd, player1.getId());
            assertThat(service().getMustAttackIndices(gd, player1.getId(), attackable))
                    .containsExactly(index(berserkers));
        }

        @Test
        @DisplayName("A forced creature that cannot satisfy its group restriction may stay home")
        void forcedRestrictedCreatureMayStayHomeWhenNoLegalDeclarationIncludesIt() {
            Permanent conscripts = addCreatureReady(player1, new OrcishConscripts());
            conscripts.setMustAttackThisTurn(true);

            List<Integer> attackable = service().getAttackableCreatureIndices(gd, player1.getId());
            assertThat(service().getMustAttackIndices(gd, player1.getId(), attackable)).isEmpty();

            enterDeclareAttackers();

            assertThatCode(() -> declare(List.of())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("CR 508.1d: an attack tax suspends every requirement, since the cost is optional")
        void anAttackTaxSuspendsEveryRequirement() {
            addCreatureReady(player1, new BerserkersOfBloodRidge());
            harness.addToBattlefield(player2, new WindbornMuse());

            List<Integer> attackable = service().getAttackableCreatureIndices(gd, player1.getId());
            assertThat(attackable).hasSize(1);
            assertThat(service().getMustAttackIndices(gd, player1.getId(), attackable)).isEmpty();
        }

        @Test
        @DisplayName("Conditional attack requirements follow the selected attacker group")
        void conditionalRequirementFollowsSelectedAttackerGroup() {
            Permanent cyclops = addCreatureReady(player1, new EkunduCyclops());
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());

            List<Integer> attackable = service().getAttackableCreatureIndices(gd, player1.getId());

            assertThat(service().getMustAttackAlongsideIndices(
                    gd, player1.getId(), attackable, List.of(index(bears))))
                    .containsExactly(index(cyclops));
            assertThat(service().getMustAttackAlongsideIndices(
                    gd, player1.getId(), attackable, List.of(index(cyclops))))
                    .isEmpty();
        }

        @Test
        @DisplayName("Counter-bearer requirements follow the selected attacker group")
        void counterBearerRequirementFollowsSelectedAttackerGroup() {
            harness.addToBattlefield(player1, new MagneticWeb());
            Permanent first = addCreatureReady(player1, new GrizzlyBears());
            Permanent second = addCreatureReady(player1, new GrizzlyBears());
            Permanent unrelated = addCreatureReady(player1, new GrizzlyBears());
            first.setCounterCount(CounterType.MAGNET, 1);
            second.setCounterCount(CounterType.MAGNET, 1);

            List<Integer> attackable = service().getAttackableCreatureIndices(gd, player1.getId());

            assertThat(service().getMustAttackAlongsideIndices(
                    gd, player1.getId(), attackable, List.of(index(first))))
                    .containsExactly(index(second));
            assertThat(service().getMustAttackAlongsideIndices(
                    gd, player1.getId(), attackable, List.of(index(unrelated))))
                    .isEmpty();
        }

        @Test
        @DisplayName("CR 508.1d: a Phyrexian attack tax suspends every requirement too")
        void aPhyrexianAttackTaxSuspendsEveryRequirement() {
            addCreatureReady(player1, new BerserkersOfBloodRidge());
            harness.addToBattlefield(player2, new NornsAnnex());

            List<Integer> attackable = service().getAttackableCreatureIndices(gd, player1.getId());
            assertThat(service().getMustAttackIndices(gd, player1.getId(), attackable)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Declare-attackers step")
    class DeclareAttackersStep {

        private void handleStep() {
            harness.inMutationScope(() -> service().handleDeclareAttackersStep(gd));
        }

        @Test
        @DisplayName("With no legal attacker the step is skipped without asking the active player")
        void noLegalAttackerSkipsTheStep() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);

            handleStep();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
        }

        @Test
        @DisplayName("The opened declaration carries the attackers, the forced ones and the targets")
        void openedDeclarationCarriesTheChoices() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent berserkers = addCreatureReady(player1, new BerserkersOfBloodRidge());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);

            handleStep();

            PendingInteraction.AttackerDeclaration declaration =
                    gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class);
            assertThat(declaration).isNotNull();
            assertThat(declaration.activePlayerId()).isEqualTo(player1.getId());
            assertThat(declaration.attackerIndices()).containsExactly(index(bears), index(berserkers));
            assertThat(declaration.mustAttackIndices()).containsExactly(index(berserkers));
            assertThat(declaration.availableTargets())
                    .anyMatch(target -> target.id().equals(player2.getId()));
            assertThat(declaration.taxPerCreature()).isZero();
            assertThat(declaration.mustAttackWithAtLeastOne()).isFalse();
        }

        @Test
        @DisplayName("The declaration reports the attack tax and the forced-attack demand")
        void openedDeclarationCarriesTheTaxAndTheDemand() {
            addCreatureReady(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new WindbornMuse());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);

            handleStep();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)
                    .taxPerCreature()).isEqualTo(2);

            // Trove of Temptation forces an attack, but only while no tax makes attacking optional.
            gd.playerBattlefields.get(player2.getId()).clear();
            harness.addToBattlefield(player2, new TroveOfTemptation());
            handleStep();

            PendingInteraction.AttackerDeclaration declaration =
                    gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class);
            assertThat(declaration.taxPerCreature()).isZero();
            assertThat(declaration.mustAttackWithAtLeastOne()).isTrue();
        }
    }

    @Nested
    @DisplayName("Declaration validation")
    class DeclarationValidation {

        @Test
        @DisplayName("Declaring outside the attacker-declaration interaction is rejected")
        void declaringWithoutAPendingDeclarationIsRejected() {
            addCreatureReady(player1, new GrizzlyBears());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not awaiting attacker declaration");
        }

        @Test
        @DisplayName("Only the active player may declare attackers")
        void onlyTheActivePlayerMayDeclareAttackers() {
            addCreatureReady(player1, new GrizzlyBears());
            addCreatureReady(player2, new GrizzlyBears());
            enterDeclareAttackers();

            assertThatThrownBy(() -> harness.inMutationScope(() ->
                    service().declareAttackers(gd, player2, List.of(0), null)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only the active player can declare attackers");
        }

        @Test
        @DisplayName("The same creature may not be declared twice")
        void duplicateAttackerIndicesAreRejected() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            addCreatureReady(player1, new HillGiant());
            enterDeclareAttackers();

            int bearsIdx = index(bears);
            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(bearsIdx, bearsIdx)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Duplicate attacker indices");
        }

        @Test
        @DisplayName("A creature that can't attack at all is rejected as an attacker index")
        void ineligibleAttackerIndexIsRejected() {
            Permanent tapped = addCreatureReady(player1, new GrizzlyBears());
            tapped.tap();
            addCreatureReady(player1, new HillGiant());
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(tapped))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");
        }

        @Test
        @DisplayName("CR 508.1c: a \"can't attack alone\" creature needs a companion in the declaration")
        void cantAttackAloneCreatureNeedsACompanion() {
            Permanent familiar = addCreatureReady(player1, new JackalFamiliar());
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(familiar))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't attack alone");

            assertThatCode(() -> declare(List.of(index(familiar), index(bears))))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("\"Can only attack alone\" rejects a declaration that brings company")
        void canOnlyAttackAloneCreatureMayNotBringCompany() {
            // Errantry: the enchanted creature can only attack alone.
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            Permanent errantry = harness.addToBattlefieldAndReturn(player1, new Errantry());
            errantry.setAttachedTo(bears.getId());
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(bears), index(giant))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can only attack alone");

            assertThatCode(() -> declare(List.of(index(bears)))).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("CR 508.1a: Okk needs a strictly greater-power attacker beside it")
        void greaterPowerRestrictionNeedsABiggerAttacker() {
            // Okk is 4/4; Hill Giant (3/3) is not enough, Craw Wurm (6/4) is.
            Permanent okk = addCreatureReady(player1, new Okk());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            Permanent wurm = addCreatureReady(player1, new CrawWurm());
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(okk), index(giant))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("a creature with greater power also attacks");

            assertThatCode(() -> declare(List.of(index(okk), index(wurm)))).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("CR 508.1a: Orcish Conscripts needs the required number of other attackers")
        void countRestrictionNeedsEnoughOtherAttackers() {
            // Orcish Conscripts can't attack unless at least two other creatures attack.
            Permanent conscripts = addCreatureReady(player1, new OrcishConscripts());
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                    List.of(index(conscripts), index(bears))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("at least 2 other creatures attack");

            assertThatCode(() -> declare(List.of(index(conscripts), index(bears), index(giant))))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("CR 508.1d: a declaration must satisfy as many attack requirements as it can")
        void asManyAttackRequirementsAsPossibleMustBeSatisfied() {
            Permanent berserkers = addCreatureReady(player1, new BerserkersOfBloodRidge());
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            enterDeclareAttackers();

            // Attacking with the unconstrained creature alone leaves the forced one at home.
            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(bears))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Creature at index " + index(berserkers) + " must attack this combat");

            assertThatCode(() -> declare(List.of(index(berserkers)))).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("\"Must attack with at least one creature\" rejects an empty declaration")
        void mustAttackWithAtLeastOneCreature() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new TroveOfTemptation());
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must attack with at least one creature");

            assertThatCode(() -> declare(List.of(index(bears)))).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("An attack target that is neither the defender nor an attackable permanent is rejected")
        void anIllegalAttackTargetIsRejected() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(bears)),
                    Map.of(index(bears), UUID.randomUUID())))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attack target for attacker at index");
        }

        @Test
        @DisplayName("A creature forced to attack a specific player may not be pointed elsewhere")
        void aForcedAttackTargetIsEnforced() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            // Alluring Siren's "attacks you this turn if able" is recorded on the creature itself;
            // pointing it at its own controller leaves the defending player an illegal choice.
            bears.setMustAttackTargetId(player1.getId());
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(bears))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("must attack the specified player");
        }

        @Test
        @DisplayName("A defender-scoped restriction excludes the barred creature from declaration choices")
        void defenderScopedRestrictionExcludesBarredCreature() {
            // Form of the Dragon: "Creatures without flying can't attack you."
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent drake = addCreatureReady(player1, new WindDrake());
            harness.addToBattlefield(player2, new FormOfTheDragon());
            enterDeclareAttackers();

            assertThat(service().getAttackableCreatureIndices(gd, player1.getId()))
                    .containsExactly(index(drake));
            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(bears), index(drake))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");

            assertThatCode(() -> declare(List.of(index(drake)))).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Attack costs")
    class AttackTax {

        @Test
        @DisplayName("The tax is summed across the whole declaration and must be payable as one total")
        void taxIsSummedAcrossTheWholeDeclaration() {
            // Windborn Muse: creatures can't attack you unless their controller pays {2} for each.
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            harness.addToBattlefield(player2, new WindbornMuse());
            enterDeclareAttackers();

            List<Integer> both = List.of(index(bears), index(giant));

            // Enough for one attacker but not for two — and the failed attempt spends nothing.
            harness.addMana(player1, ManaColor.RED, 3);
            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, both))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana to pay attack tax (4 required)");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);

            harness.addMana(player1, ManaColor.RED, 1);
            harness.beginAttackerDeclarationInput();
            assertThatCode(() -> declare(both)).doesNotThrowAnyException();

            assertThat(bears.isAttacking()).isTrue();
            assertThat(giant.isAttacking()).isTrue();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("A per-creature aura tax is added on top of the board-wide one")
        void perCreatureAuraTaxAddsToTheBoardWideTax() {
            // Brainwash: the enchanted creature can't attack unless its controller pays {3}.
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            Permanent brainwash = harness.addToBattlefieldAndReturn(player2, new Brainwash());
            brainwash.setAttachedTo(bears.getId());
            harness.addToBattlefield(player2, new WindbornMuse());
            enterDeclareAttackers();

            // {2} per attacker for the Muse, plus {3} for the one enchanted creature.
            harness.addMana(player1, ManaColor.RED, 6);
            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(bears), index(giant))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("(7 required)");

            // The unenchanted creature alone owes only the Muse's {2}.
            harness.beginAttackerDeclarationInput();
            assertThatCode(() -> declare(List.of(index(giant)))).doesNotThrowAnyException();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        }

        @Test
        @DisplayName("CR 119.4: an unpayable Phyrexian tax leaves the mana pool untouched")
        void phyrexianTaxIsValidatedBeforeAnyManaIsSpent() {
            // Norn's Annex: creatures can't attack you unless their controller pays {W/P} for each.
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new NornsAnnex());
            harness.addMana(player1, ManaColor.RED, 2);
            harness.setLife(player1, 1);
            enterDeclareAttackers();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index(bears))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough life to pay Phyrexian attack tax (2 required)");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
            assertThat(bears.isAttacking()).isFalse();
        }

        @Test
        @DisplayName("A Phyrexian tax is paid with the named colour when the pool holds it, life otherwise")
        void phyrexianTaxPrefersManaOverLife() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new NornsAnnex());
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.setLife(player1, 20);
            enterDeclareAttackers();

            declare(List.of(index(bears)));

            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
            harness.assertLife(player1, 20);
        }
    }

    @Test
    @DisplayName("An accepted declaration marks and taps the attackers, clears the prompt and logs once")
    void acceptedDeclarationMarksAndTapsTheAttackers() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent bystander = addCreatureReady(player1, new HillGiant());
        enterDeclareAttackers();

        assertThat(declare(List.of(index(bears), index(angel)))).isEqualTo(CombatResult.AUTO_PASS_ONLY);

        assertThat(bears.isAttacking()).isTrue();
        assertThat(bears.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(bears.isTapped()).isTrue();
        // CR 702.20b: vigilance means attacking doesn't cause the creature to tap.
        assertThat(angel.isAttacking()).isTrue();
        assertThat(angel.isTapped()).isFalse();
        assertThat(bystander.isAttacking()).isFalse();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
        assertThat(gameLogContains("declares 2 attackers.")).isTrue();
        assertThat(service().getAttackingCreatureIndices(gd, player1.getId()))
                .containsExactly(index(bears), index(angel));
    }

    @Test
    @DisplayName("An empty declaration is accepted and attacks with nothing")
    void emptyDeclarationIsAccepted() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enterDeclareAttackers();

        assertThat(declare(List.of())).isEqualTo(CombatResult.AUTO_PASS_ONLY);

        assertThat(bears.isAttacking()).isFalse();
        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
        assertThat(gameLogContains("declares no attackers.")).isTrue();
    }
}
