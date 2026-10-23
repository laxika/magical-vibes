package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AbzanAdvantage;
import com.github.laxika.magicalvibes.cards.a.ArchfiendOfDepravity;
import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.a.AshnodsAltar;
import com.github.laxika.magicalvibes.cards.a.AuntieOolCursewretch;
import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
import com.github.laxika.magicalvibes.cards.c.CouncilsJudgment;
import com.github.laxika.magicalvibes.cards.c.Curfew;
import com.github.laxika.magicalvibes.cards.d.DesecrationDemon;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.d.DivineReckoning;
import com.github.laxika.magicalvibes.cards.d.Duneblast;
import com.github.laxika.magicalvibes.cards.e.Expropriate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HatchetBully;
import com.github.laxika.magicalvibes.cards.h.HighPerfectMorcant;
import com.github.laxika.magicalvibes.cards.h.HumbleDefector;
import com.github.laxika.magicalvibes.cards.i.ImperialEdict;
import com.github.laxika.magicalvibes.cards.i.InnocentBlood;
import com.github.laxika.magicalvibes.cards.n.Nevermore;
import com.github.laxika.magicalvibes.cards.n.NulltreadGargantuan;
import com.github.laxika.magicalvibes.cards.p.PathOfPeace;
import com.github.laxika.magicalvibes.cards.p.PatternMatcher;
import com.github.laxika.magicalvibes.cards.p.PromiseOfLoyalty;
import com.github.laxika.magicalvibes.cards.r.RavenousVampire;
import com.github.laxika.magicalvibes.cards.r.RedHerringPlaytest;
import com.github.laxika.magicalvibes.cards.s.ScarscaleRitual;
import com.github.laxika.magicalvibes.cards.s.ShadowUrchin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SlipOnTheRing;
import com.github.laxika.magicalvibes.cards.s.SoulImmolation;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TheDiningCar;
import com.github.laxika.magicalvibes.cards.t.ThelonsChant;
import com.github.laxika.magicalvibes.cards.t.TrapTheTrespassers;
import com.github.laxika.magicalvibes.cards.t.TurfWar;
import com.github.laxika.magicalvibes.cards.v.Vault11VotersDilemma;
import com.github.laxika.magicalvibes.cards.w.WildUnraveling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimpseTheUnthinkablePlaytest.class, PathOfPeace.class, DivineReckoning.class, Nevermore.class,
        DiabolicEdict.class, InnocentBlood.class, GrizzlyBears.class, PromiseOfLoyalty.class, Duneblast.class,
        ArchfiendOfDepravity.class, AshnodsAltar.class, BoneSplinters.class, TurfWar.class,
        AshayaSoulOfTheWild.class, Forest.class, TheDiningCar.class, ImperialEdict.class, Curfew.class,
        NulltreadGargantuan.class, DesecrationDemon.class, RavenousVampire.class, AbzanAdvantage.class,
        SlipOnTheRing.class, ShadowUrchin.class, HighPerfectMorcant.class, AuntieOolCursewretch.class,
        Shock.class, ScarscaleRitual.class, SoulImmolation.class, GristleGlutton.class, HatchetBully.class,
        WildUnraveling.class, ThelonsChant.class, Swamp.class, CouncilsJudgment.class, TrapTheTrespassers.class,
        HumbleDefector.class, Vault11VotersDilemma.class, Expropriate.class, RedHerringPlaytest.class,
        PatternMatcher.class})
class GlimpseTheUnthinkablePlaytestTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents spells from targeting Glimpse, the Unthinkable")
    void shroudPreventsTargeting() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(
                player1, new GlimpseTheUnthinkablePlaytest());

        harness.setHand(player2, List.of(new PathOfPeace()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, glimpse.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Divine Reckoning cannot keep Glimpse even when it is the only creature")
    void cannotBeChosenToSurviveDivineReckoning() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new DivineReckoning(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player1, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Glimpse's name cannot be chosen while it is on the battlefield")
    void cannotChooseNameWhileOnBattlefield() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new Nevermore(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Glimpse, the Unthinkable"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertNotOnBattlefield(player1, "Nevermore");
    }

    @Test
    @DisplayName("Glimpse's name can be chosen while it is only in a player's hand")
    void canChooseNameWhileOffBattlefield() {
        harness.setHand(player2, List.of(new GlimpseTheUnthinkablePlaytest()));

        harness.castFromHand(player1, new Nevermore(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Glimpse, the Unthinkable");

        harness.assertOnBattlefield(player1, "Nevermore");
        assertThat(findPermanent(player1, "Nevermore").getChosenName())
                .isEqualTo("Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("An edict can't make its target sacrifice Glimpse when it is their only creature")
    void edictCannotChooseGlimpseAsOnlyCreature() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An edict takes the other creature without a prompt when Glimpse is the alternative")
    void edictTakesOtherCreatureAutomatically() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each-player sacrifice skips a player whose only creature is Glimpse")
    void eachPlayerSacrificeSkipsGlimpse() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new InnocentBlood(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Card-name suggestions leave out Glimpse's name while it is on the battlefield")
    void nameSuggestionsOmitGlimpseOnBattlefield() throws Exception {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.clearMessages();

        harness.castFromHand(player1, new Nevermore(), "{1}{W}{W}");
        harness.passBothPriorities();

        var prompts = harness.getConn1().getMessagesContaining("LIST_PICK");
        assertThat(prompts).isNotEmpty();
        var options = new JacksonConfig().objectMapper().readTree(prompts.getLast()).get("options");
        List<String> names = new java.util.ArrayList<>();
        options.forEach(option -> names.add(option.asString()));
        assertThat(names).contains("Grizzly Bears").doesNotContain("Glimpse, the Unthinkable");

        harness.handleListChoice(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A keep-one-then-sacrifice-the-rest effect sacrifices Glimpse when it is the only creature")
    void keepOneEffectSacrificesGlimpseAsOnlyCreature() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        castPromiseOfLoyalty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player2, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("A keep-one effect keeps the other creature without a prompt and sacrifices Glimpse")
    void keepOneEffectKeepsOtherCreatureAutomatically() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPromiseOfLoyalty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
        assertThat(bears.getCounterCount(CounterType.VOW)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Choose-up-to-one-then-destroy-the-rest destroys Glimpse without a prompt when it is the only creature")
    void chooseUpToOneDestroyRestDestroysGlimpse() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new Duneblast(), "{4}{W}{B}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Keeping up to two creatures can't keep Glimpse, so it is sacrificed with the rest")
    void keepUpToTwoCannotKeepGlimpse() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player2, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(glimpse.getCard());
    }

    @Test
    @DisplayName("Keeping up to two creatures sacrifices Glimpse without a prompt when it is the only creature")
    void keepUpToTwoSacrificesGlimpseAsOnlyCreature() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("A sacrifice-a-creature activation cost can't be paid with Glimpse when it is the only creature")
    void activationSacrificeCostCannotChooseGlimpse() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Glimpse, the Unthinkable");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A sacrifice-a-creature activation cost takes the other creature without a prompt")
    void activationSacrificeCostTakesOtherCreatureAutomatically() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Glimpse can't be chosen to pay a spell's additional sacrifice cost")
    void spellSacrificeCostCannotChooseGlimpse() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), glimpse.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Glimpse, the Unthinkable");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Turf War can't give control of Glimpse when it is the only contested land")
    void turfWarCannotChooseGlimpseAsOnlyContestedLand() {
        harness.addToBattlefield(player1, new TurfWar());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());
        glimpse.setCounterCount(CounterType.CONTESTED, 1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(glimpse);
    }

    @Test
    @DisplayName("Turf War takes the other contested land without a prompt when Glimpse is the alternative")
    void turfWarTakesOtherContestedLandAutomatically() {
        harness.addToBattlefield(player1, new TurfWar());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());
        glimpse.setCounterCount(CounterType.CONTESTED, 1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.CONTESTED, 1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(glimpse);
    }

    @Test
    @DisplayName("Turf War's enter trigger can't choose Glimpse as the land to get a contested counter")
    void turfWarEnterTriggerSkipsGlimpse() {
        Permanent ashaya = harness.addToBattlefieldAndReturn(player1, new AshayaSoulOfTheWild());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new TurfWar()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(glimpse.getCounterCount(CounterType.CONTESTED)).isZero();
        assertThat(ashaya.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
        assertThat(opponentForest.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Dining Car can't make you sacrifice Glimpse when it alone has the least toughness")
    void diningCarCannotChooseGlimpseWithLeastToughness() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());

        resolveDiningCarUpkeep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("The Dining Car sacrifices the creature tied with Glimpse for least toughness without a prompt")
    void diningCarTakesTiedCreatureAutomatically() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        resolveDiningCarUpkeep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Imperial Edict can't make its target choose Glimpse when it is their only creature")
    void imperialEdictCannotChooseGlimpseAsOnlyCreature() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Imperial Edict destroys the other creature without a prompt when Glimpse is the alternative")
    void imperialEdictTakesOtherCreatureAutomatically() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Curfew can't make a player return Glimpse when it is their only creature")
    void curfewCannotChooseGlimpse() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new Curfew(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Nulltread Gargantuan tops itself without a prompt when Glimpse is the only other creature")
    void nulltreadGargantuanCannotChooseGlimpse() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new NulltreadGargantuan(), "{1}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Glimpse, the Unthinkable");
        harness.assertNotOnBattlefield(player1, "Nulltread Gargantuan");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Nulltread Gargantuan");
    }

    @Test
    @DisplayName("Desecration Demon can't be fed Glimpse when it is the opponent's only creature")
    void desecrationDemonCannotSacrificeGlimpse() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
        assertThat(demon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ravenous Vampire sacrifices itself without a prompt when Glimpse is the other nonartifact creature")
    void ravenousVampireCannotSacrificeGlimpse() {
        harness.addToBattlefield(player1, new RavenousVampire());
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player1, "Ravenous Vampire");
    }

    @Test
    @DisplayName("Bolster can't choose Glimpse when it alone has the least toughness")
    void bolsterCannotChooseGlimpseWithLeastToughness() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        castAbzanAdvantage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(glimpse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bolster picks the creature tied with Glimpse for least toughness without a prompt")
    void bolsterTakesTiedCreatureAutomatically() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castAbzanAdvantage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(glimpse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Ring can't choose Glimpse as Ring-bearer, so the other creature is chosen without a prompt")
    void ringCannotChooseGlimpseAsRingBearer() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.ringStates.get(player1.getId()).bearerId())
                .isEqualTo(findPermanent(player1, "Grizzly Bears").getId());
    }

    @Test
    @DisplayName("A mandatory blight puts the counter on the other creature without a prompt")
    void blightTakesOtherCreatureAutomatically() {
        Permanent urchin = addCreatureReady(player1, new ShadowUrchin());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(urchin.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent whose only creature is Glimpse can't blight")
    void eachOpponentBlightsSkipsGlimpse() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new HighPerfectMorcant(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ward—Blight counters the spell when the caster's only creature is Glimpse")
    void wardBlightCountersSpellWhenOnlyGlimpse() {
        Permanent auntie = harness.addToBattlefieldAndReturn(player1, new AuntieOolCursewretch());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player2, new GlimpseTheUnthinkablePlaytest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, auntie.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Auntie Ool, Cursewretch");
        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Glimpse can't be chosen to pay a spell's put-a-counter additional cost")
    void spellCounterCostCannotChooseGlimpse() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());
        harness.setHand(player1, List.of(new ScarscaleRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, glimpse.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Glimpse can't be chosen to pay a spell's blight additional cost")
    void spellBlightCostCannotChooseGlimpse() {
        Permanent glimpse = addCreatureReady(player1, new GlimpseTheUnthinkablePlaytest());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null,
                List.of(), List.of(), false, glimpse.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Glimpse can't be chosen to pay the counters option of a counters-or-mana additional cost")
    void countersOrManaCostCannotChooseGlimpse() {
        Card spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        Permanent glimpse = harness.addToBattlefieldAndReturn(player2, new GlimpseTheUnthinkablePlaytest());
        harness.setHand(player2, List.of(new WildUnraveling()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player2, 0, spell.getId(), glimpse.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An activated blight cost blights the other creature without a prompt")
    void activatedBlightCostTakesOtherCreatureAutomatically() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent glutton = addCreatureReady(player1, new GristleGlutton());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(glutton.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An activated put-a-counter cost puts the counter on the other creature without a prompt")
    void activatedCounterCostTakesOtherCreatureAutomatically() {
        Permanent bully = addCreatureReady(player1, new HatchetBully());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bully.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("\"Unless they put a -1/-1 counter on a creature\" deals damage without a prompt when Glimpse is the only creature")
    void unlessPutCounterPunishesWhenOnlyGlimpse() {
        harness.addToBattlefield(player2, new ThelonsChant());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player1, new GlimpseTheUnthinkablePlaytest());

        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 17);
        assertThat(glimpse.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Will of the council can't vote for Glimpse, so every vote goes to the other permanent")
    void willOfTheCouncilCannotVoteForGlimpse() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Will of the council exiles nothing when Glimpse is the only permanent to vote for")
    void willOfTheCouncilWithOnlyGlimpseExilesNothing() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new CouncilsJudgment(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Secret council can't vote for Glimpse, so every vote goes to the other creature")
    void secretCouncilCannotVoteForGlimpse() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(player2, new GlimpseTheUnthinkablePlaytest());
        Permanent defector = harness.addToBattlefieldAndReturn(player2, new HumbleDefector());

        harness.castFromHand(player1, new TrapTheTrespassers(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(defector.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(glimpse.getCounterCount(CounterType.STUN)).isZero();
        assertThat(glimpse.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A vote for up to one creature skips the vote when Glimpse is the only creature")
    void voteForCreatureSkipsGlimpse() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault11VotersDilemma());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize + 1);
    }

    @Test
    @DisplayName("Expropriate's money vote can't take Glimpse, so the voter's other permanent is taken")
    void expropriateCannotChooseGlimpse() {
        Card glimpseCard = new GlimpseTheUnthinkablePlaytest();
        glimpseCard.setOwnerId(player2.getId());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player2, glimpseCard);
        Card bearsCard = new GrizzlyBears();
        bearsCard.setOwnerId(player2.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, bearsCard);
        harness.setHand(player1, List.of(new Expropriate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleListChoice(player1, ChoiceContext.ExpropriateChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.ExpropriateChoice.MONEY);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(glimpse);
    }

    @Test
    @DisplayName("Red Herring exchanges with the other permanent without a prompt when Glimpse is the alternative")
    void redHerringTakesOtherPermanentAutomatically() {
        Permanent glimpse = addCreatureReady(player1, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bears.getCard().getName()).isEqualTo("Red Herring");
        assertThat(glimpse.getCard().getName()).isEqualTo("Glimpse, the Unthinkable");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(redHerring);
    }

    @Test
    @DisplayName("Red Herring can't exchange with Glimpse when it is the only permanent you control")
    void redHerringCannotExchangeWithGlimpse() {
        Permanent glimpse = addCreatureReady(player1, new GlimpseTheUnthinkablePlaytest());
        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(glimpse.getCard().getName()).isEqualTo("Glimpse, the Unthinkable");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(redHerring);
    }

    @Test
    @DisplayName("Pattern Matcher can find a card named Glimpse when Glimpse is the other creature you control")
    void patternMatcherFindsGlimpseByName() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());
        harness.setLibrary(player1, List.of(new GlimpseTheUnthinkablePlaytest(), new Forest()));

        castPatternMatcher();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Pattern Matcher searches for the names of all other creatures, Glimpse included, without a prompt")
    void patternMatcherSearchesGlimpseAndOtherCreatureNames() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GlimpseTheUnthinkablePlaytest(), new GrizzlyBears(), new Forest()));

        castPatternMatcher();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactlyInAnyOrder("Glimpse, the Unthinkable", "Grizzly Bears");
    }

    private void castPatternMatcher() {
        harness.setHand(player1, List.of(new PatternMatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castAbzanAdvantage() {
        harness.setHand(player1, List.of(new AbzanAdvantage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    private void resolveDiningCarUpkeep() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheDiningCar(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();
    }

    private void castPromiseOfLoyalty() {
        harness.setHand(player1, List.of(new PromiseOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
