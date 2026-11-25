package com.github.laxika.magicalvibes.prevention;

import com.github.laxika.magicalvibes.cards.b.BloatedToad;
import com.github.laxika.magicalvibes.cards.b.BurnTheImpure;
import com.github.laxika.magicalvibes.cards.c.ChantOfVituGhazi;
import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.f.FerventPaincaster;
import com.github.laxika.magicalvibes.cards.g.GhyrsonStarnKelermorph;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaBurst;
import com.github.laxika.magicalvibes.cards.p.Planeswalkerificate;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetSpellDamagePreventionShield;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.Prevention;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Damage to a permanent from the any-target records (Shock, Prodigal Pyromancer, Ghyrson Starn) and from the
 * target-creature record (Burn the Impure, Fervent Paincaster's exert ability) goes through one path: every
 * prevention is an option of the damaged permanent's pool, so both prevent the same way and the permanent's
 * controller orders two of them (CR 616.1).
 */
@CardUsed({BloatedToad.class, BurnTheImpure.class, GhyrsonStarnKelermorph.class, WhiteKnight.class, ChantOfVituGhazi.class, Combust.class, FerventPaincaster.class, GrizzlyBears.class,
        LavaBurst.class, Planeswalkerificate.class, ProdigalPyromancer.class, RodOfRuin.class, Shock.class,
        SilverKnight.class})
class PermanentDamagePathTest extends BaseCardTest {

    private static final String PERMANENT_SHIELD = "Prevent the next damage to it";

    @Nested
    @DisplayName("A prevention of damage from red sources")
    class SourcePrevention {

        @Test
        @DisplayName("Prevents an any-target spell's damage to a creature")
        void anyTargetSpell() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            shock(bears.getId());

            assertUndamaged(bears);
            assertLogged("Shock's damage is prevented.");
        }

        @Test
        @DisplayName("Prevents a target-creature spell's damage")
        void targetCreatureSpell() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            burnTheImpure(bears.getId());

            assertUndamaged(bears);
            assertLogged("Burn the Impure's damage is prevented.");
        }
    }

    @Nested
    @DisplayName("A shield against a targeted spell's damage")
    class TargetSpellShield {

        @Test
        @DisplayName("Prevents an any-target spell's damage to a creature")
        void anyTargetSpell() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Card shock = new Shock();
            gd.addShield(new TargetSpellDamagePreventionShield(shock.getId(), null));

            cast(shock, bears.getId());

            assertUndamaged(bears);
            assertLogged("Shock's damage is prevented.");
        }

        @Test
        @DisplayName("Prevents a target-creature spell's damage")
        void targetCreatureSpell() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Card burn = new BurnTheImpure();
            gd.addShield(new TargetSpellDamagePreventionShield(burn.getId(), null));

            cast(burn, bears.getId());

            assertUndamaged(bears);
            assertLogged("Burn the Impure's damage is prevented.");
        }

        @Test
        @DisplayName("A life-gaining shield gains its player the any-target spell's prevented damage")
        void anyTargetSpellGainsLife() {
            harness.setLife(player2, 20);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Card shock = new Shock();
            gd.addShield(new TargetSpellDamagePreventionShield(shock.getId(), player2.getId()));

            cast(shock, bears.getId());

            assertUndamaged(bears);
            harness.assertLife(player2, 22);
        }

        @Test
        @DisplayName("A life-gaining shield gains its player the target-creature spell's prevented damage")
        void targetCreatureSpellGainsLife() {
            harness.setLife(player2, 20);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Card burn = new BurnTheImpure();
            gd.addShield(new TargetSpellDamagePreventionShield(burn.getId(), player2.getId()));

            cast(burn, bears.getId());

            assertUndamaged(bears);
            harness.assertLife(player2, 23);
        }
    }

    @Nested
    @DisplayName("Chant of Vitu-Ghazi")
    class ChantOfVituGhaziPrevention {

        @Test
        @DisplayName("Prevents a creature's any-target ability damage to a creature and gains its caster that much life")
        void anyTargetAbility() {
            harness.setLife(player2, 20);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            castChant();

            pyromancerPings(pyromancer, bears.getId());

            assertUndamaged(bears);
            harness.assertLife(player2, 21);
        }

        @Test
        @DisplayName("Prevents a creature's target-creature ability damage and gains its caster that much life")
        void targetCreatureAbility() {
            harness.setLife(player2, 20);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent paincaster = addCreatureReady(player1, new FerventPaincaster());
            castChant();

            paincasterPings(paincaster, bears.getId());

            assertUndamaged(bears);
            harness.assertLife(player2, 21);
        }
    }

    @Nested
    @DisplayName("A prevention of all damage creatures would deal")
    class CreaturePrevention {

        @Test
        @DisplayName("Prevents a creature's any-target ability damage without asking for an order")
        void anyTargetAbility() {
            Prevention.add(gd, PreventDamageEffect.allByCreatures());
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

            pyromancerPings(pyromancer, bears.getId());

            assertThat(orderPrompt()).isNull();
            assertUndamaged(bears);
        }

        @Test
        @DisplayName("Prevents a creature's target-creature ability damage without asking for an order")
        void targetCreatureAbility() {
            Prevention.add(gd, PreventDamageEffect.allByCreatures());
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent paincaster = addCreatureReady(player1, new FerventPaincaster());

            paincasterPings(paincaster, bears.getId());

            assertThat(orderPrompt()).isNull();
            assertUndamaged(bears);
        }

        @Test
        @DisplayName("An opponent-only prevention still prevents an any-target ability's damage after its creature left")
        void opponentOnlyAnyTargetAfterSourceLeft() {
            Prevention.addOnPlayer(gd, PreventDamageEffect.allByOpponentCreatures(), player2.getId());
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

            harness.activateAbility(player1, indexOf(player1, pyromancer), null, bears.getId());
            killInResponse(pyromancer);
            harness.passBothPriorities();

            assertUndamaged(bears);
        }

        @Test
        @DisplayName("An opponent-only prevention still prevents a target-creature ability's damage after its creature left")
        void opponentOnlyTargetCreatureAfterSourceLeft() {
            Prevention.addOnPlayer(gd, PreventDamageEffect.allByOpponentCreatures(), player2.getId());
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent paincaster = addCreatureReady(player1, new FerventPaincaster());

            harness.activateAbility(player1, indexOf(player1, paincaster), 1, null, bears.getId());
            killInResponse(paincaster);
            harness.passBothPriorities();

            assertUndamaged(bears);
        }
    }

    @Nested
    @DisplayName("A creature prevented from dealing damage")
    class SourcePreventedFromDealing {

        @Test
        @DisplayName("Deals no damage with its any-target ability")
        void anyTargetAbility() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            Prevention.addOnPermanent(gd, PreventDamageEffect.allByTargetCreatures(), pyromancer.getId());

            pyromancerPings(pyromancer, bears.getId());

            assertUndamaged(bears);
            assertLogged("Prodigal Pyromancer's damage is prevented.");
        }

        @Test
        @DisplayName("Deals no damage with its target-creature ability")
        void targetCreatureAbility() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent paincaster = addCreatureReady(player1, new FerventPaincaster());
            Prevention.addOnPermanent(gd, PreventDamageEffect.allByTargetCreatures(), paincaster.getId());

            paincasterPings(paincaster, bears.getId());

            assertUndamaged(bears);
            assertLogged("Fervent Paincaster's damage is prevented.");
        }
    }

    /**
     * A targeted spell or ability can't reach a creature with protection from it (the target is illegal, CR 702.16b,
     * 608.2b), so protection's damage prevention (CR 702.16e) is reached by damage to a recipient that isn't a target:
     * Ghyrson Starn's damage to the permanent another source dealt exactly 1 damage.
     */
    @Nested
    @DisplayName("Protection")
    class Protection {

        @Test
        @DisplayName("Prevents an any-target ability's damage to a creature from a source of the stated quality")
        void anyTargetAbility() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());

            rodThenGhyrson(knight);
            harness.passBothPriorities();

            assertThat(knight.getMarkedDamage()).isEqualTo(1);
            harness.assertOnBattlefield(player2, "Silver Knight");
        }

        @Test
        @DisplayName("Prevents damage to a creature whose toughness is its loyalty")
        void toughnessAsLoyaltyRecipient() {
            Permanent toad = addCreatureReady(player1, new BloatedToad());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player1, List.of(new Planeswalkerificate()));
            harness.addMana(player1, ManaColor.RED, 4);
            harness.castEnchantment(player1, 0, toad.getId());
            harness.passBothPriorities();
            resolveAllTriggers();
            addCreatureReady(player2, new GhyrsonStarnKelermorph());
            Permanent rod = addCreatureReady(player2, new RodOfRuin());

            harness.forceActivePlayer(player2);
            harness.addMana(player2, ManaColor.COLORLESS, 3);
            harness.activateAbility(player2, indexOf(player2, rod), null, toad.getId());
            harness.passBothPriorities();
            assertThat(gqs.getEffectiveToughness(gd, toad)).isEqualTo(1);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Bloated Toad");
            assertThat(gqs.getEffectiveToughness(gd, toad)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Damage that can't be prevented")
    class Unpreventable {

        @Test
        @DisplayName("An any-target ability's damage ignores protection while damage can't be prevented")
        void anyTargetAbilityIgnoresProtection() {
            gd.addShield(new DamageCantBePreventedThisTurnEffect());
            Permanent knight = addCreatureReady(player2, new SilverKnight());

            rodThenGhyrson(knight);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Silver Knight");
        }

        @Test
        @DisplayName("Combust's damage that can't be prevented is dealt through a shield without reducing it")
        void combustThroughShield() {
            Permanent knight = addCreatureReady(player2, new WhiteKnight());
            gd.addPermanentDamagePreventionShield(knight.getId(), 5);

            harness.setHand(player1, List.of(new Combust()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castInstant(player1, 0, knight.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "White Knight");
            assertThat(gd.permanentDamagePreventionShield(knight.getId())).isEqualTo(5);
        }

        @Test
        @DisplayName("Lava Burst's damage to a creature is dealt through a shield without reducing it")
        void lavaBurstThroughShield() {
            Permanent bears = shieldedBears();

            harness.setHand(player1, List.of(new LavaBurst()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.castSorcery(player1, 0, 2, bears.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(5);
        }

        @Test
        @DisplayName("Chant of Vitu-Ghazi prevents nothing and gains no life for an any-target ability")
        void chantAnyTargetAbility() {
            harness.setLife(player2, 20);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            castChant();
            gd.addShield(new DamageCantBePreventedThisTurnEffect());

            pyromancerPings(pyromancer, bears.getId());

            assertThat(bears.getMarkedDamage()).isEqualTo(1);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Chant of Vitu-Ghazi prevents nothing and gains no life for a target-creature ability")
        void chantTargetCreatureAbility() {
            harness.setLife(player2, 20);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent paincaster = addCreatureReady(player1, new FerventPaincaster());
            castChant();
            gd.addShield(new DamageCantBePreventedThisTurnEffect());

            paincasterPings(paincaster, bears.getId());

            assertThat(bears.getMarkedDamage()).isEqualTo(1);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("A life-gaining spell shield prevents nothing and gains no life")
        void lifeGainingSpellShield() {
            harness.setLife(player2, 20);
            gd.addShield(new DamageCantBePreventedThisTurnEffect());
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Card shock = new Shock();
            gd.addShield(new TargetSpellDamagePreventionShield(shock.getId(), player2.getId()));

            cast(shock, bears.getId());

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("A prevention shield on the creature isn't reduced by damage that can't be prevented")
        void shieldNotReduced() {
            gd.addShield(new DamageCantBePreventedThisTurnEffect());
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            gd.addPermanentDamagePreventionShield(bears.getId(), 5);

            burnTheImpure(bears.getId());

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(5);
        }
    }

    /**
     * CR 616.1: when a second prevention applies to the same damage, the damaged creature's controller chooses the
     * order; choosing the creature's own shield first spends it.
     */
    @Nested
    @DisplayName("Ordering two preventions of damage to a creature")
    class Ordering {

        @Test
        @DisplayName("A red-source prevention and a shield: an any-target spell asks the creature's controller")
        void sourcePreventionAnyTarget() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            Permanent bears = shieldedBears();

            shock(bears.getId());

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(3);
            assertUndamaged(bears);
        }

        @Test
        @DisplayName("A red-source prevention and a shield: a target-creature spell asks the creature's controller")
        void sourcePreventionTargetCreature() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            Permanent bears = shieldedBears();

            burnTheImpure(bears.getId());

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(2);
            assertUndamaged(bears);
        }

        @Test
        @DisplayName("A targeted-spell shield and a creature shield: an any-target spell asks the creature's controller")
        void targetSpellShieldAnyTarget() {
            Permanent bears = shieldedBears();
            Card shock = new Shock();
            gd.addShield(new TargetSpellDamagePreventionShield(shock.getId(), null));

            cast(shock, bears.getId());

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(3);
            assertUndamaged(bears);
        }

        @Test
        @DisplayName("A targeted-spell shield and a creature shield: a target-creature spell asks the creature's controller")
        void targetSpellShieldTargetCreature() {
            Permanent bears = shieldedBears();
            Card burn = new BurnTheImpure();
            gd.addShield(new TargetSpellDamagePreventionShield(burn.getId(), null));

            cast(burn, bears.getId());

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(2);
            assertUndamaged(bears);
        }

        @Test
        @DisplayName("Chant of Vitu-Ghazi and a shield: an any-target ability asks; the shield first leaves no life to gain")
        void chantAnyTarget() {
            harness.setLife(player2, 20);
            Permanent bears = shieldedBears();
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            castChant();

            pyromancerPings(pyromancer, bears.getId());

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(4);
            assertUndamaged(bears);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Chant of Vitu-Ghazi and a shield: a target-creature ability asks; the shield first leaves no life to gain")
        void chantTargetCreature() {
            harness.setLife(player2, 20);
            Permanent bears = shieldedBears();
            Permanent paincaster = addCreatureReady(player1, new FerventPaincaster());
            castChant();

            paincasterPings(paincaster, bears.getId());

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(4);
            assertUndamaged(bears);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("A source prevented from dealing damage and a shield: an any-target ability asks")
        void sourcePreventedAnyTarget() {
            Permanent bears = shieldedBears();
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            Prevention.addOnPermanent(gd, PreventDamageEffect.allByTargetCreatures(), pyromancer.getId());

            pyromancerPings(pyromancer, bears.getId());

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(4);
        }

        @Test
        @DisplayName("A source prevented from dealing damage and a shield: a target-creature ability asks")
        void sourcePreventedTargetCreature() {
            Permanent bears = shieldedBears();
            Permanent paincaster = addCreatureReady(player1, new FerventPaincaster());
            Prevention.addOnPermanent(gd, PreventDamageEffect.allByTargetCreatures(), paincaster.getId());

            paincasterPings(paincaster, bears.getId());

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(4);
        }

        @Test
        @DisplayName("Protection and a shield: an any-target ability asks the creature's controller")
        void protectionAnyTarget() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());

            rodThenGhyrson(knight);
            gd.addPermanentDamagePreventionShield(knight.getId(), 5);
            harness.passBothPriorities();

            chooseShieldFirst();
            assertThat(gd.permanentDamagePreventionShield(knight.getId())).isEqualTo(3);
            assertThat(knight.getMarkedDamage()).isEqualTo(1);
        }

        @Test
        @DisplayName("Protection first keeps the creature's shield whole")
        void protectionFirstKeepsShield() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());

            rodThenGhyrson(knight);
            gd.addPermanentDamagePreventionShield(knight.getId(), 5);
            harness.passBothPriorities();

            PendingInteraction.ColorChoice choice = orderPrompt();
            assertThat(choice).isNotNull();
            harness.handleListChoice(player2, choice.options().stream()
                    .filter(option -> !option.startsWith(PERMANENT_SHIELD))
                    .findFirst().orElseThrow());
            assertThat(gd.permanentDamagePreventionShield(knight.getId())).isEqualTo(5);
            assertThat(knight.getMarkedDamage()).isEqualTo(1);
        }
    }

    /** The any-target path's player branch leaves its preventions to the player's damage pool too (CR 616.1). */
    @Nested
    @DisplayName("Damage to a player")
    class PlayerRecipient {

        @Test
        @DisplayName("A red-source prevention prevents an any-target spell's damage to a player")
        void sourcePrevention() {
            harness.setLife(player2, 20);
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));

            shock(player2.getId());

            harness.assertLife(player2, 20);
            assertLogged("Shock's damage to ");
        }

        @Test
        @DisplayName("A targeted-spell shield prevents an any-target spell's damage to a player")
        void targetSpellShield() {
            harness.setLife(player2, 20);
            Card shock = new Shock();
            gd.addShield(new TargetSpellDamagePreventionShield(shock.getId(), null));

            cast(shock, player2.getId());

            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("A red-source prevention and the player's shield: the player orders them")
        void sourcePreventionAndShield() {
            harness.setLife(player2, 20);
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            gd.addPlayerDamagePreventionShield(player2.getId(), 5);

            shock(player2.getId());

            choosePlayerShieldFirst();
            assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(3);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("A targeted-spell shield and the player's shield: the player orders them")
        void targetSpellShieldAndShield() {
            harness.setLife(player2, 20);
            Card shock = new Shock();
            gd.addShield(new TargetSpellDamagePreventionShield(shock.getId(), null));
            gd.addPlayerDamagePreventionShield(player2.getId(), 5);

            cast(shock, player2.getId());

            choosePlayerShieldFirst();
            assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(3);
            harness.assertLife(player2, 20);
        }

        private void choosePlayerShieldFirst() {
            PendingInteraction.ColorChoice choice = orderPrompt();
            assertThat(choice).as("the player is asked to order the preventions").isNotNull();
            assertThat(choice.playerId()).isEqualTo(player2.getId());
            harness.handleListChoice(player2, choice.options().stream()
                    .filter(option -> option.startsWith("Prevent the next"))
                    .findFirst().orElseThrow());
        }
    }

    /** Rod of Ruin deals 1 damage to {@code recipient}; Ghyrson Starn's trigger to deal it 2 more waits on the stack. */
    private void rodThenGhyrson(Permanent recipient) {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        Permanent rod = addCreatureReady(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, rod), null, recipient.getId());
        harness.passBothPriorities();
        assertThat(recipient.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent shieldedBears() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        gd.addPermanentDamagePreventionShield(bears.getId(), 5);
        return bears;
    }

    private PendingInteraction.ColorChoice orderPrompt() {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        return choice != null && choice.context() instanceof ChoiceContext.ReplacementEffectOrder ? choice : null;
    }

    private void chooseShieldFirst() {
        PendingInteraction.ColorChoice choice = orderPrompt();
        assertThat(choice).as("the creature's controller is asked to order the preventions").isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, choice.options().stream()
                .filter(option -> option.startsWith(PERMANENT_SHIELD))
                .findFirst().orElseThrow());
    }

    private void assertUndamaged(Permanent permanent) {
        assertThat(permanent.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(permanent);
    }

    private void assertLogged(String text) {
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(line -> line.contains(text));
    }

    private void shock(UUID targetId) {
        cast(new Shock(), targetId);
    }

    private void burnTheImpure(UUID targetId) {
        cast(new BurnTheImpure(), targetId);
    }

    private void cast(Card spell, UUID targetId) {
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void castChant() {
        harness.castFromHand(player2, new ChantOfVituGhazi(), "{6}{W}{W}");
        harness.passBothPriorities();
    }

    private void pyromancerPings(Permanent pyromancer, UUID targetId) {
        harness.ensurePriority(player1);
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, targetId);
        harness.passBothPriorities();
    }

    private void paincasterPings(Permanent paincaster, UUID targetId) {
        harness.ensurePriority(player1);
        harness.activateAbility(player1, indexOf(player1, paincaster), 1, null, targetId);
        harness.passBothPriorities();
    }

    private void killInResponse(Permanent creature) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
