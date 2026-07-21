package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.Befuddle;
import com.github.laxika.magicalvibes.cards.a.AgonyWarp;
import com.github.laxika.magicalvibes.cards.b.BenalishMarshal;
import com.github.laxika.magicalvibes.cards.c.ContagionClasp;
import com.github.laxika.magicalvibes.cards.d.Diminish;
import com.github.laxika.magicalvibes.cards.f.FeelingOfDread;
import com.github.laxika.magicalvibes.cards.f.FulgentDistraction;
import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.f.FalkenrathNoble;
import com.github.laxika.magicalvibes.cards.f.FertileGround;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantAmbushBeetle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KarnsTemporalSundering;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.b.BloodcrazedNeonate;
import com.github.laxika.magicalvibes.cards.n.Nekrataal;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.cards.p.Pounce;
import com.github.laxika.magicalvibes.cards.q.QuicksilverGeyser;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Skulduggery;
import com.github.laxika.magicalvibes.cards.s.SlaveOfBolas;
import com.github.laxika.magicalvibes.cards.s.SplendidAgony;
import com.github.laxika.magicalvibes.cards.s.Stun;
import com.github.laxika.magicalvibes.cards.s.SynchronizedStrike;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.cards.w.WizardsLightning;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@Tag("scryfall")
class AiTargetSelectorTest {

    private GameTestHarness harness;
    private Player human;
    private Player aiPlayer;
    private GameData gd;
    private AiTargetSelector targetSelector;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        human = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();

        targetSelector = new AiTargetSelector(
                harness.getGameQueryService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
    }

    // ===== chooseTarget: mana-ramp land auras =====

    @Test
    @DisplayName("Mana-ramp land aura enchants the AI's own land, not the opponent's")
    void manaRampLandAuraTargetsOwnLand() {
        harness.addToBattlefield(human, new Forest());
        Permanent ownLand = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());

        UUID target = targetSelector.chooseTarget(gd, new FertileGround(), aiPlayer.getId());

        assertThat(target).isEqualTo(ownLand.getId());
    }

    @Test
    @DisplayName("Wild Growth also enchants the AI's own land, not the opponent's")
    void wildGrowthTargetsOwnLand() {
        harness.addToBattlefield(human, new Forest());
        Permanent ownLand = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());

        UUID target = targetSelector.chooseTarget(gd, new WildGrowth(), aiPlayer.getId());

        assertThat(target).isEqualTo(ownLand.getId());
    }

    @Test
    @DisplayName("Mana-ramp land aura prefers an untapped land so the ramp is usable this turn")
    void manaRampLandAuraPrefersUntappedLand() {
        Permanent tappedLand = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());
        tappedLand.tap();
        Permanent untappedLand = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());

        UUID target = targetSelector.chooseTarget(gd, new FertileGround(), aiPlayer.getId());

        assertThat(target).isEqualTo(untappedLand.getId());
    }

    @Test
    @DisplayName("Mana-ramp land aura picks no target when the AI controls no land")
    void manaRampLandAuraNoTargetWhenNoOwnLand() {
        harness.addToBattlefield(human, new Forest());

        UUID target = targetSelector.chooseTarget(gd, new FertileGround(), aiPlayer.getId());

        assertThat(target).isNull();
    }

    // ===== chooseTarget: forced own-board removal fallback =====

    @Test
    @DisplayName("Nekrataal's ETB kill targets the opponent's legal creature when one exists")
    void nekrataalTargetsOpponentCreature() {
        Permanent oppBears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        harness.addToBattlefield(aiPlayer, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new Nekrataal(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppBears.getId());
    }

    @Test
    @DisplayName("Forced onto its own board, Nekrataal's ETB kill picks the AI's weakest creature, not its best")
    void nekrataalForcedSelfTargetPicksWeakestOwnCreature() {
        // Opponent's only creature is black — not a legal Nekrataal target, so the
        // mandatory ETB kill falls back to the AI's own board.
        harness.addToBattlefield(human, new FalkenrathNoble());
        harness.addToBattlefield(aiPlayer, new SerraAngel());
        Permanent ownElves = harness.addToBattlefieldAndReturn(aiPlayer, new LlanowarElves());

        UUID target = targetSelector.chooseTarget(gd, new Nekrataal(), aiPlayer.getId());

        assertThat(target).isEqualTo(ownElves.getId());
    }

    // ===== chooseTarget: damage spells =====

    @Test
    @DisplayName("Damage spell targets the opponent's creature, not the AI's own")
    void damageSpellTargetsOpponentCreatureNotOwnBoard() {
        // Regression: damage used to fall through to the general fallback, whose
        // own-battlefield-first search made the AI burn its own creatures.
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        Card damageSpell = new Card();
        damageSpell.setName("Test Shock");
        damageSpell.setType(CardType.INSTANT);
        damageSpell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));

        UUID target = targetSelector.chooseTarget(gd, damageSpell, aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Any-target damage goes to the opponent's face when their board is empty")
    void anyTargetDamageGoesFaceWhenOpponentBoardEmpty() {
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());

        Card damageSpell = new Card();
        damageSpell.setName("Test Shock");
        damageSpell.setType(CardType.INSTANT);
        damageSpell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));

        UUID target = targetSelector.chooseTarget(gd, damageSpell, aiPlayer.getId());

        assertThat(target).isEqualTo(human.getId());
    }

    @Test
    @DisplayName("Creature-only damage forced onto the AI's own board picks its weakest creature")
    void creatureDamageForcedSelfTargetPicksWeakestOwnCreature() {
        // No opponent creatures and no player fallback — a mandatory damage target can
        // only come from the AI's own board, so give up the least valuable creature.
        harness.addToBattlefield(aiPlayer, new SerraAngel());
        Permanent ownElves = harness.addToBattlefieldAndReturn(aiPlayer, new LlanowarElves());

        Card damageSpell = new Card();
        damageSpell.setName("Test Creature Burn");
        damageSpell.setType(CardType.INSTANT);
        damageSpell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));

        UUID target = targetSelector.chooseTarget(gd, damageSpell, aiPlayer.getId());

        assertThat(target).isEqualTo(ownElves.getId());
    }

    // ===== chooseTarget: bounce removal =====

    @Test
    @DisplayName("Bounce spell targets the opponent's creature, not the AI's own permanents")
    void bounceTargetsOpponentCreatureNotOwnBoard() {
        // Regression: bounce used to fall through to the general fallback, whose
        // own-battlefield-first search made Quicksilver Geyser bounce the AI's own permanent.
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new QuicksilverGeyser(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Exile-then removal (Path to Exile) targets the opponent's creature, not the AI's own")
    void exileThenRemovalTargetsOpponentCreature() {
        Permanent oppBears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        harness.addToBattlefield(aiPlayer, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new PathToExile(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppBears.getId());
    }

    @Test
    @DisplayName("Bounce with no legal opponent target falls back to the AI's least valuable permanent")
    void bounceForcedSelfTargetPicksLeastValuable() {
        harness.addToBattlefield(aiPlayer, new SerraAngel());
        Permanent ownElves = harness.addToBattlefieldAndReturn(aiPlayer, new LlanowarElves());

        UUID target = targetSelector.chooseTarget(gd, new QuicksilverGeyser(), aiPlayer.getId());

        assertThat(target).isEqualTo(ownElves.getId());
    }

    // ===== chooseTarget: harmful disruption (tap, can't-block, debuff, steal) =====

    @Test
    @DisplayName("Tap spell targets the opponent's most threatening creature, not the AI's own")
    void tapSpellTargetsOpponentThreatNotOwnBoard() {
        // Regression: tap-downs used to fall through to the general fallback, whose
        // own-battlefield-first search made the AI tap its own creatures.
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        harness.addToBattlefield(human, new LlanowarElves());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        Card tapSpell = new Card();
        tapSpell.setName("Test Tap");
        tapSpell.setType(CardType.INSTANT);
        tapSpell.addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));

        UUID target = targetSelector.chooseTarget(gd, tapSpell, aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Tap spell with an empty opponent board falls back to the AI's least valuable creature")
    void tapSpellForcedSelfTargetPicksLeastValuable() {
        harness.addToBattlefield(aiPlayer, new SerraAngel());
        Permanent ownElves = harness.addToBattlefieldAndReturn(aiPlayer, new LlanowarElves());

        Card tapSpell = new Card();
        tapSpell.setName("Test Tap");
        tapSpell.setType(CardType.INSTANT);
        tapSpell.addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));

        UUID target = targetSelector.chooseTarget(gd, tapSpell, aiPlayer.getId());

        assertThat(target).isEqualTo(ownElves.getId());
    }

    @Test
    @DisplayName("Can't-block spell (Stun) targets the opponent's creature, not the AI's own")
    void cantBlockSpellTargetsOpponentCreatureNotOwnBoard() {
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new Stun(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Debuff spell (Befuddle, -4/-0) targets the opponent's creature, not the AI's own")
    void debuffSpellTargetsOpponentCreatureNotOwnBoard() {
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new Befuddle(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Steal spell (Act of Treason) targets the opponent's creature, not the AI's own")
    void stealSpellTargetsOpponentCreatureNotOwnBoard() {
        // Act of Treason has no controller restriction on its target filter, so the general
        // fallback would happily "steal" the AI's own creature. Its leading untap effect must
        // also not classify the card as beneficial.
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new ActOfTreason(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("-1/-1 counter ETB (Contagion Clasp) targets the opponent's creature, not the AI's own")
    void minusCounterEtbTargetsOpponentCreatureNotOwnBoard() {
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new ContagionClasp(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Base-P/T shrink (Diminish) targets the opponent's creature, not the AI's own")
    void basePowerToughnessShrinkTargetsOpponentCreature() {
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new Diminish(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Steal-and-sacrifice spell (Slave of Bolas) targets the opponent's creature, not the AI's own")
    void stealAndSacrificeSpellTargetsOpponentCreatureNotOwnBoard() {
        // The sacrifice-at-end-step rider classifies the card as removal, so it must aim at
        // the opponent's best creature rather than the AI stealing (and sacrificing) its own.
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new SlaveOfBolas(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Forced-block ETB (Giant Ambush Beetle) targets the opponent's creature, not the AI's own")
    void forcedBlockEtbTargetsOpponentCreatureNotOwnBoard() {
        // The may-wrapped MustBlockSourceEffect is harmful to the blocker it targets — it must
        // not fall into the own-battlefield-first fallback and force the AI's own creature to block.
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

        UUID target = targetSelector.chooseTarget(gd, new GiantAmbushBeetle(), aiPlayer.getId());

        assertThat(target).isEqualTo(oppAngel.getId());
    }

    @Test
    @DisplayName("Positive pump spell still targets the AI's own creature first")
    void positivePumpSpellStillTargetsOwnBoard() {
        // The harmful-disruption classification is sign-aware: a Giant Growth-shaped boost
        // must keep the own-battlefield-first fallback, not get routed at the opponent.
        Permanent ownBears = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        harness.addToBattlefield(human, new SerraAngel());

        Card pumpSpell = new Card();
        pumpSpell.setName("Test Growth");
        pumpSpell.setType(CardType.INSTANT);
        pumpSpell.addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));

        UUID target = targetSelector.chooseTarget(gd, pumpSpell, aiPlayer.getId());

        assertThat(target).isEqualTo(ownBears.getId());
    }

    // ===== findValidPermanentTargetsForManaValueX =====

    @Test
    @DisplayName("Returns creatures with mana value within affordable range")
    void returnsCreaturesWithAffordableManaValue() {
        // EliteVanguard MV=1, GrizzlyBears MV=2
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 2);

        assertThat(targets).hasSize(2);
    }

    @Test
    @DisplayName("Excludes creatures with mana value exceeding maxX")
    void excludesCreaturesExceedingMaxX() {
        // EliteVanguard MV=1, GrizzlyBears MV=2
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 1);

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }

    @Test
    @DisplayName("Returns empty list when no creatures have affordable mana value")
    void returnsEmptyWhenNoAffordableTargets() {
        // GrizzlyBears MV=2, but maxX=1
        harness.addToBattlefield(human, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        // maxX=0 means nothing is affordable (MV must be >= 1)
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 0);

        assertThat(targets).isEmpty();
    }

    @Test
    @DisplayName("Returns empty list when no creatures on any battlefield")
    void returnsEmptyWhenNoCreatures() {
        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).isEmpty();
    }

    @Test
    @DisplayName("Includes creatures from both opponent and own battlefield")
    void includesCreaturesFromBothBattlefields() {
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).hasSize(2);
    }

    @Test
    @DisplayName("Searches opponent battlefield before own")
    void searchesOpponentFirst() {
        harness.addToBattlefield(human, new EliteVanguard());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        // Opponent's creature should be listed before own
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }

    @Test
    @DisplayName("Excludes non-creature permanents")
    void excludesNonCreatures() {
        // Island is a land, not a creature — should be excluded by the card's target filter
        harness.addToBattlefield(human, new Island());
        harness.addToBattlefield(human, new EliteVanguard());

        EntrancingMelody card = new EntrancingMelody();
        List<Permanent> targets = targetSelector.findValidPermanentTargetsForManaValueX(
                gd, card, aiPlayer.getId(), 5);

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().getCard().getName()).isEqualTo("Elite Vanguard");
    }

    // ===== "any target" spells: isValidPermanentTarget =====

    @Test
    @DisplayName("Rejects non-creature artifact for 'any target' damage spell")
    void rejectsArtifactForAnyTargetSpell() {
        // Elaborate Firecannon is a non-creature artifact — not a valid target for "any target" damage
        Permanent artifact = harness.addToBattlefieldAndReturn(human, new ElaborateFirecannon());

        WizardsLightning spell = new WizardsLightning();
        assertThat(targetSelector.isValidPermanentTarget(gd, spell, artifact, aiPlayer.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("Rejects land for 'any target' damage spell")
    void rejectsLandForAnyTargetSpell() {
        Permanent land = harness.addToBattlefieldAndReturn(human, new Island());

        WizardsLightning spell = new WizardsLightning();
        assertThat(targetSelector.isValidPermanentTarget(gd, spell, land, aiPlayer.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("Accepts creature for 'any target' damage spell")
    void acceptsCreatureForAnyTargetSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());

        WizardsLightning spell = new WizardsLightning();
        assertThat(targetSelector.isValidPermanentTarget(gd, spell, creature, aiPlayer.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("chooseTarget picks creature, not non-creature permanent, for 'any target' spell")
    void chooseTargetSkipsNonCreatureForAnyTargetSpell() {
        // Opponent has a land and a creature — AI should only target the creature
        harness.addToBattlefield(human, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());

        WizardsLightning spell = new WizardsLightning();
        UUID targetId = targetSelector.chooseTarget(gd, spell, aiPlayer.getId());

        assertThat(targetId).isEqualTo(bears.getId());
    }

    // ===== findValidGraveyardTargets: type filtering =====

    private static Card makeGraveyardCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private static Card makeBasicLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.setSupertypes(Set.of(CardSupertype.BASIC));
        return card;
    }

    private void setupGraveyardWithAllTypes() {
        harness.setGraveyard(human, List.of(
                makeGraveyardCard("GY Creature", CardType.CREATURE),
                makeGraveyardCard("GY Instant", CardType.INSTANT),
                makeGraveyardCard("GY Sorcery", CardType.SORCERY),
                makeGraveyardCard("GY Artifact", CardType.ARTIFACT),
                makeGraveyardCard("GY Enchantment", CardType.ENCHANTMENT),
                makeBasicLand("GY Basic Land")
        ));
    }

    static Stream<Arguments> graveyardEffectFilterCases() {
        return Stream.of(
                Arguments.of(
                        "PutCreatureFromOpponentGraveyard filters to creatures only",
                        new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect(),
                        Set.of("GY Creature")
                ),
                Arguments.of(
                        "CastTargetInstantOrSorceryFromGraveyard filters to instants and sorceries",
                        new CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope.OPPONENT_GRAVEYARD, false),
                        Set.of("GY Instant", "GY Sorcery")
                ),
                Arguments.of(
                        "ExileGraveyardCards(CREATURE) filters to creatures only",
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                new CardTypePredicate(CardType.CREATURE)),
                        Set.of("GY Creature")
                ),
                Arguments.of(
                        "ExileGraveyardCards(null) allows all card types",
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD),
                        Set.of("GY Creature", "GY Instant", "GY Sorcery", "GY Artifact", "GY Enchantment", "GY Basic Land")
                ),
                Arguments.of(
                        "GrantFlashbackToTargetGraveyardCard ignores the opponent's graveyard (controller-only)",
                        new GrantFlashbackToTargetGraveyardCardEffect(Set.of(CardType.INSTANT, CardType.SORCERY)),
                        Set.of()
                ),
                Arguments.of(
                        "ExileTargetCardFromGraveyardAndImprint(ARTIFACT) filters to artifacts only",
                        new ExileTargetCardFromGraveyardAndImprintOnSourceEffect(new CardTypePredicate(CardType.ARTIFACT)),
                        Set.of("GY Artifact")
                ),
                Arguments.of(
                        "PutCardFromOpponentGraveyard filters to artifacts and creatures",
                        new PutCardFromOpponentGraveyardOntoBattlefieldEffect(),
                        Set.of("GY Creature", "GY Artifact")
                ),
                Arguments.of(
                        "ExileTargetGraveyardCardAndSameName excludes basic lands",
                        new ExileTargetGraveyardCardAndSameNameFromZonesEffect(),
                        Set.of("GY Creature", "GY Instant", "GY Sorcery", "GY Artifact", "GY Enchantment")
                ),
                Arguments.of(
                        "ExileGraveyardCardWithConditionalBonus allows all card types",
                        new ExileGraveyardCardWithConditionalBonusEffect(3, 1, 1),
                        Set.of("GY Creature", "GY Instant", "GY Sorcery", "GY Artifact", "GY Enchantment", "GY Basic Land")
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("graveyardEffectFilterCases")
    @DisplayName("findValidGraveyardTargets filters by effect type")
    void findValidGraveyardTargets_filtersByEffectType(String description, CardEffect effect, Set<String> expectedNames) {
        setupGraveyardWithAllTypes();

        Card spellCard = new Card();
        spellCard.setName("Test Spell");
        spellCard.setType(CardType.SORCERY);
        spellCard.addEffect(EffectSlot.SPELL, effect);

        List<Card> results = targetSelector.findValidGraveyardTargets(gd, spellCard, aiPlayer.getId());

        Set<String> resultNames = results.stream().map(Card::getName).collect(java.util.stream.Collectors.toSet());
        assertThat(resultNames).isEqualTo(expectedNames);
    }

    @Test
    @DisplayName("findValidGraveyardTargets searches own graveyard for controller-only effects")
    void findValidGraveyardTargets_controllerOnlyEffectUsesOwnGraveyard() {
        setupGraveyardWithAllTypes(); // opponent's graveyard — must be ignored
        harness.setGraveyard(aiPlayer, List.of(
                makeGraveyardCard("Own GY Instant", CardType.INSTANT),
                makeGraveyardCard("Own GY Creature", CardType.CREATURE)
        ));

        Card spellCard = new Card();
        spellCard.setName("Test Spell");
        spellCard.setType(CardType.SORCERY);
        spellCard.addEffect(EffectSlot.SPELL,
                new GrantFlashbackToTargetGraveyardCardEffect(Set.of(CardType.INSTANT, CardType.SORCERY)));

        List<Card> results = targetSelector.findValidGraveyardTargets(gd, spellCard, aiPlayer.getId());

        assertThat(results).extracting(Card::getName).containsExactly("Own GY Instant");
    }

    @Test
    @DisplayName("findValidGraveyardTargets returns empty when no cards match filter")
    void findValidGraveyardTargets_emptyWhenNoMatch() {
        // Only non-creature cards in graveyard
        harness.setGraveyard(human, List.of(
                makeGraveyardCard("GY Instant", CardType.INSTANT),
                makeBasicLand("GY Basic Land")
        ));

        Card spellCard = new Card();
        spellCard.setName("Test Spell");
        spellCard.setType(CardType.SORCERY);
        spellCard.addEffect(EffectSlot.SPELL, new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect());

        List<Card> results = targetSelector.findValidGraveyardTargets(gd, spellCard, aiPlayer.getId());

        assertThat(results).isEmpty();
    }

    // =====================================================================
    // Mockito-based tests for buildDamageAssignments, computeBaseAllowedTargets,
    // and EffectResolution.needsDamageDistribution
    // =====================================================================

    @Nested
    @DisplayName("buildDamageAssignments (unit)")
    class BuildDamageAssignmentsUnit {

        private GameQueryService mockGqs;
        private TargetValidationService mockTvs;
        private AiTargetSelector unitSelector;
        private GameData unitGd;
        private UUID aiId;
        private UUID opponentId;

        @BeforeEach
        void setUp() {
            mockGqs = mock(GameQueryService.class);
            mockTvs = mock(TargetValidationService.class);
            // The shared spell-target core delegates structural legality to TargetLegalityService;
            // stub it to report every candidate structurally legal so the distribution logic runs.
            TargetLegalityService mockTls = mock(TargetLegalityService.class);
            lenient().when(mockTls.checkSpellPermanentTargetableReason(any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            unitSelector = new AiTargetSelector(mockGqs, mockTvs, mockTls);

            aiId = UUID.randomUUID();
            opponentId = UUID.randomUUID();
            unitGd = new GameData(UUID.randomUUID(), "test", aiId, "AI");
            unitGd.orderedPlayerIds.add(aiId);
            unitGd.orderedPlayerIds.add(opponentId);
            unitGd.playerIds.add(aiId);
            unitGd.playerIds.add(opponentId);
            unitGd.playerBattlefields.put(aiId, Collections.synchronizedList(new ArrayList<>()));
            unitGd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));

            // Default: all effect-level target validations pass
            lenient().when(mockTvs.checkEffectTargets(any(), any())).thenReturn(Optional.empty());
        }

        private Permanent addCreature(UUID owner, String name, int toughness) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            Permanent perm = new Permanent(card);
            unitGd.playerBattlefields.get(owner).add(perm);
            lenient().when(mockGqs.isCreature(unitGd, perm)).thenReturn(true);
            lenient().when(mockGqs.getEffectiveToughness(unitGd, perm)).thenReturn(toughness);
            return perm;
        }

        @Test
        @DisplayName("Returns null for spell with no divided damage effect")
        void returnsNullForNonDividedDamageSpell() {
            Card bolt = new Card();
            bolt.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            assertThat(unitSelector.buildDamageAssignments(unitGd, bolt, aiId)).isNull();
        }

        @Test
        @DisplayName("Distributes creature-only divided damage to opponent's creatures")
        void distributesCreatureOnlyDividedDamage() {
            addCreature(opponentId, "Bear", 2);

            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.target(1, 3).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).isNotNull();
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(3);
        }

        @Test
        @DisplayName("Returns null for creature-only divided damage when no valid creatures")
        void returnsNullForCreatureOnlyWithNoTargets() {
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.target(1, 3).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));

            assertThat(unitSelector.buildDamageAssignments(unitGd, spell, aiId)).isNull();
        }

        @Test
        @DisplayName("Handles CHOSEN any-targets DealDividedDamageEffect inside ConditionalReplacementEffect")
        void handlesAnyTargetDividedDamageFromKicker() {
            addCreature(opponentId, "Bear", 2);

            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(), 
                    new DealDamageToTargetCreatureEffect(5),
                    DealDividedDamageEffect.chosenAmongAnyTargets(10)));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).isNotNull();
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(10);
        }

        @Test
        @DisplayName("Any-target divided damage sends remaining damage to opponent player")
        void anyTargetDumpsRemainingOnOpponent() {
            addCreature(opponentId, "Bear", 2); // needs 2 lethal, remaining 8 → opponent

            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(10));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).containsEntry(opponentId, 8);
        }

        @Test
        @DisplayName("Any-target divided damage targets opponent when no creatures exist")
        void anyTargetTargetsOpponentWhenNoCreatures() {
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(10));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result).containsEntry(opponentId, 10);
        }

        @Test
        @DisplayName("Any-target divided damage kills multiple creatures then dumps remainder on opponent")
        void anyTargetKillsMultipleCreaturesThenDumpsOnOpponent() {
            addCreature(opponentId, "Goblin", 1);  // 1 lethal
            addCreature(opponentId, "Bear", 2);    // 2 lethal → remaining 7 on opponent

            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(10));

            Map<UUID, Integer> result = unitSelector.buildDamageAssignments(unitGd, spell, aiId);

            assertThat(result).isNotNull();
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(10);
            assertThat(result).containsEntry(opponentId, 7);
        }
    }

    // ===== chooseTarget: player hexproof / shroud =====

    @Nested
    @DisplayName("chooseTarget player hexproof / shroud (unit)")
    class ChooseTargetPlayerHexproofShroudUnit {

        private GameQueryService mockGqs;
        private TargetValidationService mockTvs;
        private AiTargetSelector unitSelector;
        private GameData unitGd;
        private UUID aiId;
        private UUID opponentId;

        @BeforeEach
        void setUp() {
            mockGqs = mock(GameQueryService.class);
            mockTvs = mock(TargetValidationService.class);
            unitSelector = new AiTargetSelector(mockGqs, mockTvs, null);

            aiId = UUID.randomUUID();
            opponentId = UUID.randomUUID();
            unitGd = new GameData(UUID.randomUUID(), "test", aiId, "AI");
            unitGd.orderedPlayerIds.add(aiId);
            unitGd.orderedPlayerIds.add(opponentId);
            unitGd.playerIds.add(aiId);
            unitGd.playerIds.add(opponentId);
            unitGd.playerBattlefields.put(aiId, Collections.synchronizedList(new ArrayList<>()));
            unitGd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));

            lenient().when(mockTvs.checkEffectTargets(any(), any())).thenReturn(Optional.empty());
        }

        private Card makePlayerOnlySpell() {
            Card spell = new Card();
            spell.setName("Test Player Spell");
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER));
            return spell;
        }

        @Test
        @DisplayName("Returns null when opponent has hexproof")
        void returnsNullWhenOpponentHasHexproof() {
            lenient().when(mockGqs.playerHasShroud(unitGd, opponentId)).thenReturn(false);
            lenient().when(mockGqs.playerHasHexproof(unitGd, opponentId)).thenReturn(true);

            UUID target = unitSelector.chooseTarget(unitGd, makePlayerOnlySpell(), aiId);

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Returns null when opponent has shroud")
        void returnsNullWhenOpponentHasShroud() {
            lenient().when(mockGqs.playerHasShroud(unitGd, opponentId)).thenReturn(true);

            UUID target = unitSelector.chooseTarget(unitGd, makePlayerOnlySpell(), aiId);

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Returns opponent when opponent has no hexproof or shroud")
        void returnsOpponentWhenNoProtection() {
            lenient().when(mockGqs.playerHasShroud(unitGd, opponentId)).thenReturn(false);
            lenient().when(mockGqs.playerHasHexproof(unitGd, opponentId)).thenReturn(false);

            UUID target = unitSelector.chooseTarget(unitGd, makePlayerOnlySpell(), aiId);

            assertThat(target).isEqualTo(opponentId);
        }
    }

    // ===== computeBaseAllowedTargets =====

    @Nested
    @DisplayName("computeBaseAllowedTargets (unit)")
    class ComputeBaseAllowedTargetsUnit {

        private AiTargetSelector unitSelector;

        @BeforeEach
        void setUp() {
            unitSelector = new AiTargetSelector(
                    mock(GameQueryService.class), mock(TargetValidationService.class), null);
        }

        @Test
        @DisplayName("ConditionalReplacementEffect uses only base effect targeting")
        void kickerReplacementUsesBaseOnly() {
            // Base: creature only (canTargetPermanent=true, canTargetPlayer=false)
            // Kicked: any targets (canTargetPermanent=true, canTargetPlayer=true)
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(), 
                    new DealDamageToTargetCreatureEffect(5),
                    DealDividedDamageEffect.chosenAmongAnyTargets(10)));

            Set<TargetType> allowed = unitSelector.computeBaseAllowedTargets(spell);

            assertThat(allowed).contains(TargetType.PERMANENT);
            assertThat(allowed).doesNotContain(TargetType.PLAYER);
        }

        @Test
        @DisplayName("Non-wrapped any-target effect includes both PERMANENT and PLAYER")
        void nonWrappedAnyTargetIncludesBoth() {
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            Set<TargetType> allowed = unitSelector.computeBaseAllowedTargets(spell);

            assertThat(allowed).contains(TargetType.PERMANENT);
            assertThat(allowed).contains(TargetType.PLAYER);
        }

        @Test
        @DisplayName("Creature-only effect includes only PERMANENT")
        void creatureOnlyIncludesPermanent() {
            Card spell = new Card();
            spell.setType(CardType.SORCERY);
            spell.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

            Set<TargetType> allowed = unitSelector.computeBaseAllowedTargets(spell);

            assertThat(allowed).contains(TargetType.PERMANENT);
            assertThat(allowed).doesNotContain(TargetType.PLAYER);
        }
    }

    // ===== EffectResolution.needsDamageDistribution =====

    @Nested
    @DisplayName("needsDamageDistribution (unit)")
    class NeedsDamageDistributionUnit {

        @Test
        @DisplayName("Returns true for CHOSEN among-target-creatures DealDividedDamageEffect")
        void trueForCreatureOnlyDividedDamage() {
            Card spell = new Card();
            spell.addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));

            assertThat(EffectResolution.needsDamageDistribution(spell)).isTrue();
        }

        @Test
        @DisplayName("Returns true for direct CHOSEN any-targets DealDividedDamageEffect")
        void trueForDirectAnyTargetDividedDamage() {
            Card spell = new Card();
            spell.addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(5));

            assertThat(EffectResolution.needsDamageDistribution(spell)).isTrue();
        }

        @Test
        @DisplayName("Returns false for regular damage spell")
        void falseForRegularDamageSpell() {
            Card spell = new Card();
            spell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            assertThat(EffectResolution.needsDamageDistribution(spell)).isFalse();
        }

        @Test
        @DisplayName("Returns false when effect is wrapped in ConditionalReplacementEffect")
        void falseForWrappedKickerEffect() {
            // The DealDividedDamageEffect is inside a ConditionalReplacementEffect,
            // so needsDamageDistribution should return false (the wrapper type doesn't match)
            Card spell = new Card();
            spell.addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(), 
                    new DealDamageToTargetCreatureEffect(5),
                    DealDividedDamageEffect.chosenAmongAnyTargets(10)));

            assertThat(EffectResolution.needsDamageDistribution(spell)).isFalse();
        }
    }

    // ===== chooseMultiTargets =====

    @Nested
    @DisplayName("chooseMultiTargets")
    class ChooseMultiTargetsTests {

        @Test
        @DisplayName("Karn's Temporal Sundering: picks self for extra turn + opponent's nonland permanent")
        void karnsTemporalSunderingPicksSelfAndOpponentPermanent() {
            GrizzlyBears oppCreature = new GrizzlyBears();
            harness.addToBattlefield(human, oppCreature);

            KarnsTemporalSundering card = new KarnsTemporalSundering();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(2);
            // First target: AI player (beneficial extra turn)
            assertThat(targets.get(0)).isEqualTo(aiPlayer.getId());
            // Second target: opponent's creature
            Permanent oppPerm = gd.playerBattlefields.get(human.getId()).getFirst();
            assertThat(targets.get(1)).isEqualTo(oppPerm.getId());
        }

        @Test
        @DisplayName("Karn's Temporal Sundering: returns player only when no nonland permanents exist")
        void karnsTemporalSunderingOptionalSecondTarget() {
            // No nonland permanents on either battlefield
            KarnsTemporalSundering card = new KarnsTemporalSundering();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            // Second target is optional (min=0), so should still succeed with just the player
            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(1);
            assertThat(targets.get(0)).isEqualTo(aiPlayer.getId());
        }

        @Test
        @DisplayName("Karn's Temporal Sundering: skips lands for second target")
        void karnsTemporalSunderingSkipsLands() {
            // Only a land on the battlefield — not a valid nonland permanent target
            harness.addToBattlefield(human, new Island());

            KarnsTemporalSundering card = new KarnsTemporalSundering();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(1); // Only player, no valid nonland permanent
            assertThat(targets.get(0)).isEqualTo(aiPlayer.getId());
        }

        @Test
        @DisplayName("Pounce: picks own creature and opponent's creature for fight")
        void pouncePicksOwnAndOpponentCreature() {
            EliteVanguard ownCreature = new EliteVanguard();
            harness.addToBattlefield(aiPlayer, ownCreature);
            GrizzlyBears oppCreature = new GrizzlyBears();
            harness.addToBattlefield(human, oppCreature);

            Pounce card = new Pounce();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(2);
            // First target: own creature (ControlledPermanentPredicateTargetFilter)
            Permanent ownPerm = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                    .findFirst().orElseThrow();
            assertThat(targets.get(0)).isEqualTo(ownPerm.getId());
            // Second target: opponent's creature
            Permanent oppPerm = gd.playerBattlefields.get(human.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            assertThat(targets.get(1)).isEqualTo(oppPerm.getId());
        }

        @Test
        @DisplayName("Up-to-two tap spell (Feeling of Dread) taps the opponent's two biggest threats")
        void upToTwoTapSpellTapsTopTwoOpponentThreats() {
            // Regression: single-group "up to N" spells used to take the single-target
            // path and submit only one target.
            harness.addToBattlefield(aiPlayer, new GrizzlyBears());
            Permanent oppElves = harness.addToBattlefieldAndReturn(human, new LlanowarElves());
            Permanent oppBears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
            Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

            List<UUID> targets = targetSelector.chooseMultiTargets(gd, new FeelingOfDread(), aiPlayer.getId());

            assertThat(targets).containsExactlyInAnyOrder(oppAngel.getId(), oppBears.getId());
            assertThat(targets).doesNotContain(oppElves.getId());
        }

        @Test
        @DisplayName("Up-to-two tap spell does not pad optional targets from the AI's own board")
        void upToTwoTapSpellDoesNotPadFromOwnBoard() {
            harness.addToBattlefield(aiPlayer, new SerraAngel());
            harness.addToBattlefield(aiPlayer, new LlanowarElves());
            Permanent oppBears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());

            List<UUID> targets = targetSelector.chooseMultiTargets(gd, new FeelingOfDread(), aiPlayer.getId());

            assertThat(targets).containsExactly(oppBears.getId());
        }

        @Test
        @DisplayName("Beneficial up-to-two pump picks the AI's own creatures, not the opponent's")
        void beneficialUpToTwoPumpPicksOwnCreatures() {
            Permanent ownBears = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
            Permanent ownVanguard = harness.addToBattlefieldAndReturn(aiPlayer, new EliteVanguard());
            Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

            Card pumpSpell = new Card();
            pumpSpell.setName("Test Mass Pump");
            pumpSpell.setType(CardType.INSTANT);
            pumpSpell.target(new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature"), 0, 2)
                    .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));

            List<UUID> targets = targetSelector.chooseMultiTargets(gd, pumpSpell, aiPlayer.getId());

            assertThat(targets).containsExactlyInAnyOrder(ownBears.getId(), ownVanguard.getId());
            assertThat(targets).doesNotContain(oppAngel.getId());
        }

        @Test
        @DisplayName("-1/-1 counter distribution (Splendid Agony) picks opponent creatures, not the AI's own")
        void harmfulCounterDistributionPicksOpponentCreatures() {
            Permanent oppBears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
            Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());
            Permanent ownElves = harness.addToBattlefieldAndReturn(aiPlayer, new LlanowarElves());

            List<UUID> targets = targetSelector.chooseMultiTargets(gd, new SplendidAgony(), aiPlayer.getId());

            assertThat(targets).containsExactlyInAnyOrder(oppBears.getId(), oppAngel.getId());
            assertThat(targets).doesNotContain(ownElves.getId());
        }

        @Test
        @DisplayName("Targeted untap-plus-pump (Synchronized Strike) picks the AI's own creatures")
        void beneficialUntapPumpPicksOwnCreatures() {
            Permanent ownBears = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
            Permanent ownVanguard = harness.addToBattlefieldAndReturn(aiPlayer, new EliteVanguard());
            Permanent oppAngel = harness.addToBattlefieldAndReturn(human, new SerraAngel());

            List<UUID> targets = targetSelector.chooseMultiTargets(gd, new SynchronizedStrike(), aiPlayer.getId());

            assertThat(targets).containsExactlyInAnyOrder(ownBears.getId(), ownVanguard.getId());
            assertThat(targets).doesNotContain(oppAngel.getId());
        }

        @Test
        @DisplayName("Mandatory two-target tap (Fulgent Distraction) fills from the AI's board only when forced")
        void mandatoryTwoTargetTapFillsFromOwnBoardWhenForced() {
            Permanent ownElves = harness.addToBattlefieldAndReturn(aiPlayer, new LlanowarElves());
            Permanent oppBears = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());

            List<UUID> targets = targetSelector.chooseMultiTargets(gd, new FulgentDistraction(), aiPlayer.getId());

            assertThat(targets).containsExactlyInAnyOrder(oppBears.getId(), ownElves.getId());
        }

        @Test
        @DisplayName("Mandatory two-target tap returns null when only one legal target exists")
        void mandatoryTwoTargetTapReturnsNullWhenUnsatisfiable() {
            harness.addToBattlefield(aiPlayer, new LlanowarElves());

            List<UUID> targets = targetSelector.chooseMultiTargets(gd, new FulgentDistraction(), aiPlayer.getId());

            assertThat(targets).isNull();
        }

        @Test
        @DisplayName("needsMultiTargetSelection covers multi-group and single-group up-to-N spells")
        void needsMultiTargetSelectionClassifiesSpellShapes() {
            assertThat(targetSelector.needsMultiTargetSelection(new FeelingOfDread())).isTrue();
            assertThat(targetSelector.needsMultiTargetSelection(new FulgentDistraction())).isTrue();
            assertThat(targetSelector.needsMultiTargetSelection(new AgonyWarp())).isTrue();
            assertThat(targetSelector.needsMultiTargetSelection(new Stun())).isFalse();
        }

        @Test
        @DisplayName("Pounce: returns null when AI has no creature to fight with")
        void pounceReturnsNullWithoutOwnCreature() {
            // Only opponent has a creature — first target (creature you control) can't be satisfied
            harness.addToBattlefield(human, new GrizzlyBears());

            Pounce card = new Pounce();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNull();
        }

        @Test
        @DisplayName("Pounce: returns null when opponent has no creature to fight")
        void pounceReturnsNullWithoutOpponentCreature() {
            // Only AI has a creature — second target (creature you don't control) can't be satisfied
            harness.addToBattlefield(aiPlayer, new EliteVanguard());

            Pounce card = new Pounce();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNull();
        }

        @Test
        @DisplayName("Skulduggery: picks own creature for buff and opponent's creature for debuff")
        void skulduggeryPicksBothTargets() {
            EliteVanguard ownCreature = new EliteVanguard();
            harness.addToBattlefield(aiPlayer, ownCreature);
            GrizzlyBears oppCreature = new GrizzlyBears();
            harness.addToBattlefield(human, oppCreature);

            Skulduggery card = new Skulduggery();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(2);
        }

        @Test
        @DisplayName("Does not reuse the same target across groups")
        void doesNotReuseSameTarget() {
            // Only one creature on each side — targets must be distinct
            EliteVanguard ownCreature = new EliteVanguard();
            harness.addToBattlefield(aiPlayer, ownCreature);
            GrizzlyBears oppCreature = new GrizzlyBears();
            harness.addToBattlefield(human, oppCreature);

            Pounce card = new Pounce();
            List<UUID> targets = targetSelector.chooseMultiTargets(gd, card, aiPlayer.getId());

            assertThat(targets).isNotNull();
            assertThat(targets).hasSize(2);
            assertThat(targets.get(0)).isNotEqualTo(targets.get(1));
        }
    }

    // ===== chooseSpellTarget =====

    @Nested
    @DisplayName("chooseSpellTarget")
    class ChooseSpellTarget {

        @Test
        @DisplayName("Selects opponent creature spell on the stack for Cancel")
        void selectsOpponentCreatureSpellForCancel() {
            com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                    new com.github.laxika.magicalvibes.cards.c.Cancel();

            GrizzlyBears bears = new GrizzlyBears();
            StackEntry creatureSpell = new StackEntry(bears, human.getId());
            gd.stack.add(creatureSpell);

            UUID target = targetSelector.chooseSpellTarget(gd, cancel, aiPlayer.getId());

            assertThat(target).isEqualTo(bears.getId());
        }

        @Test
        @DisplayName("Returns null when stack is empty")
        void returnsNullForEmptyStack() {
            com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                    new com.github.laxika.magicalvibes.cards.c.Cancel();

            UUID target = targetSelector.chooseSpellTarget(gd, cancel, aiPlayer.getId());

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Does not target AI's own spells")
        void doesNotTargetOwnSpells() {
            com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                    new com.github.laxika.magicalvibes.cards.c.Cancel();

            GrizzlyBears bears = new GrizzlyBears();
            StackEntry ownSpell = new StackEntry(bears, aiPlayer.getId());
            gd.stack.add(ownSpell);

            UUID target = targetSelector.chooseSpellTarget(gd, cancel, aiPlayer.getId());

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Picks highest-value spell when multiple spells on stack")
        void picksHighestValueSpell() {
            com.github.laxika.magicalvibes.cards.c.Cancel cancel =
                    new com.github.laxika.magicalvibes.cards.c.Cancel();

            GrizzlyBears bears = new GrizzlyBears(); // MV=2
            SerraAngel angel = new SerraAngel(); // MV=5
            gd.stack.add(new StackEntry(bears, human.getId()));
            gd.stack.add(new StackEntry(angel, human.getId()));

            UUID target = targetSelector.chooseSpellTarget(gd, cancel, aiPlayer.getId());

            assertThat(target).isEqualTo(angel.getId());
        }

        @Test
        @DisplayName("Essence Scatter only targets creature spells")
        void essenceScatterOnlyTargetsCreatureSpells() {
            com.github.laxika.magicalvibes.cards.e.EssenceScatter scatter =
                    new com.github.laxika.magicalvibes.cards.e.EssenceScatter();

            // Sorcery spell on the stack
            WizardsLightning bolt = new WizardsLightning();
            StackEntry sorcerySpell = new StackEntry(
                    com.github.laxika.magicalvibes.model.StackEntryType.INSTANT_SPELL,
                    bolt, human.getId(), "Wizard's Lightning",
                    bolt.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL), 0);
            gd.stack.add(sorcerySpell);

            UUID target = targetSelector.chooseSpellTarget(gd, scatter, aiPlayer.getId());

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Essence Scatter targets creature spell when present")
        void essenceScatterTargetsCreatureSpell() {
            com.github.laxika.magicalvibes.cards.e.EssenceScatter scatter =
                    new com.github.laxika.magicalvibes.cards.e.EssenceScatter();

            GrizzlyBears bears = new GrizzlyBears();
            gd.stack.add(new StackEntry(bears, human.getId()));

            UUID target = targetSelector.chooseSpellTarget(gd, scatter, aiPlayer.getId());

            assertThat(target).isEqualTo(bears.getId());
        }

        @Test
        @DisplayName("Negate does not target creature spells")
        void negateDoesNotTargetCreatureSpells() {
            com.github.laxika.magicalvibes.cards.n.Negate negate =
                    new com.github.laxika.magicalvibes.cards.n.Negate();

            GrizzlyBears bears = new GrizzlyBears();
            gd.stack.add(new StackEntry(bears, human.getId()));

            UUID target = targetSelector.chooseSpellTarget(gd, negate, aiPlayer.getId());

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("TargetFilter overload selects opponent spell on the stack")
        void targetFilterOverloadSelectsOpponentSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            StackEntry creatureSpell = new StackEntry(bears, human.getId());
            gd.stack.add(creatureSpell);

            // Use null target filter (like Spiketail Hatchling — targets any spell)
            UUID target = targetSelector.chooseSpellTarget(gd, (com.github.laxika.magicalvibes.model.filter.TargetFilter) null, aiPlayer.getId());

            assertThat(target).isEqualTo(bears.getId());
        }

        @Test
        @DisplayName("TargetFilter overload returns null when no opponent spells")
        void targetFilterOverloadReturnsNullForEmptyStack() {
            UUID target = targetSelector.chooseSpellTarget(gd, (com.github.laxika.magicalvibes.model.filter.TargetFilter) null, aiPlayer.getId());

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("TargetFilter overload picks highest value among multiple spells")
        void targetFilterOverloadPicksHighestValue() {
            GrizzlyBears bears = new GrizzlyBears(); // MV=2
            SerraAngel angel = new SerraAngel(); // MV=5
            gd.stack.add(new StackEntry(bears, human.getId()));
            gd.stack.add(new StackEntry(angel, human.getId()));

            UUID target = targetSelector.chooseSpellTarget(gd, (com.github.laxika.magicalvibes.model.filter.TargetFilter) null, aiPlayer.getId());

            assertThat(target).isEqualTo(angel.getId());
        }
    }

    // ===== chooseAbilityTarget =====

    @Nested
    @DisplayName("chooseAbilityTarget")
    class ChooseAbilityTarget {

        @Test
        @DisplayName("Damage ability targets opponent's killable creature over bigger creature")
        void damageAbilityPrefersKillableCreature() {
            // Opponent has a 1/1 and a 4/4 — ability deals 1, should prefer 1/1
            Permanent elves = harness.addToBattlefieldAndReturn(human, new LlanowarElves());
            harness.addToBattlefield(human, new AirElemental());

            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());

            ActivatedAbility ability = new ActivatedAbility(true, null,
                    List.of(new DealDamageToAnyTargetEffect(1)),
                    "{T}: Deal 1 damage to any target.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            assertThat(target).isEqualTo(elves.getId());
        }

        @Test
        @DisplayName("Damage ability targets highest-power creature when none are killable")
        void damageAbilityTargetsHighestPowerWhenNoneKillable() {
            // Both creatures have toughness > 1, so neither is killable by 1 damage
            harness.addToBattlefield(human, new GrizzlyBears()); // 2/2
            Permanent angel = harness.addToBattlefieldAndReturn(human, new SerraAngel()); // 4/4

            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new EliteVanguard());

            ActivatedAbility ability = new ActivatedAbility(true, null,
                    List.of(new DealDamageToAnyTargetEffect(1)),
                    "{T}: Deal 1 damage to any target.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            // Should pick the highest-power creature (Serra Angel 4/4)
            assertThat(target).isEqualTo(angel.getId());
        }

        @Test
        @DisplayName("Damage ability falls back to opponent face when no creatures")
        void damageAbilityFallsBackToFace() {
            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());

            ActivatedAbility ability = new ActivatedAbility(true, null,
                    List.of(new DealDamageToAnyTargetEffect(1)),
                    "{T}: Deal 1 damage to any target.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            assertThat(target).isEqualTo(human.getId());
        }

        @Test
        @DisplayName("Beneficial ability targets own best creature")
        void beneficialAbilityTargetsOwnBestCreature() {
            Permanent elves = harness.addToBattlefieldAndReturn(aiPlayer, new LlanowarElves()); // 1/1
            Permanent bears = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears()); // 2/2

            // Source is separate from target candidates
            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new EliteVanguard());

            ActivatedAbility ability = new ActivatedAbility(true, null,
                    List.of(new BoostTargetCreatureEffect(2, 2)),
                    "{T}: Target creature gets +2/+2 until end of turn.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            // Should pick the best creature (GrizzlyBears 2+2=4 > LlanowarElves 1+1=2)
            assertThat(target).isEqualTo(bears.getId());
        }

        @Test
        @DisplayName("Beneficial ability does not target source permanent itself")
        void beneficialAbilityExcludesSourceFromTargets() {
            // Only creature on battlefield is the source — no valid targets
            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());

            ActivatedAbility ability = new ActivatedAbility(true, null,
                    List.of(new BoostTargetCreatureEffect(2, 2)),
                    "{T}: Target creature gets +2/+2 until end of turn.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Regenerate ability targets own best creature")
        void regenerateTargetsOwnBestCreature() {
            harness.addToBattlefield(aiPlayer, new LlanowarElves());
            Permanent bears = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());

            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new EliteVanguard());

            ActivatedAbility ability = new ActivatedAbility(false, "{G}",
                    List.of(new RegenerateEffect(true)),
                    "{G}: Regenerate target creature.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            assertThat(target).isEqualTo(bears.getId());
        }

        @Test
        @DisplayName("Returns null when no valid target exists for damage ability")
        void returnsNullWhenNoValidTargetForDamage() {
            // creature-only damage ability with no opponent creatures
            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());

            ActivatedAbility ability = new ActivatedAbility(true, null,
                    List.of(new DealDamageToTargetCreatureEffect(1)),
                    "{T}: Deal 1 damage to target creature.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            // No opponent creatures and creature-only damage can't target player
            assertThat(target).isNull();
        }

        @Test
        @DisplayName("Does not target hexproof opponent creature with harmful ability")
        void doesNotTargetHexproofCreature() {
            // Thrun has hexproof — should not be targetable by opponent's abilities
            Permanent thrun = harness.addToBattlefieldAndReturn(human,
                    new com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll());

            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());

            ActivatedAbility ability = new ActivatedAbility(true, null,
                    List.of(new DealDamageToAnyTargetEffect(1)),
                    "{T}: Deal 1 damage to any target.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            // Should fall back to opponent face, not target hexproof Thrun
            assertThat(target).isNotEqualTo(thrun.getId());
            assertThat(target).isEqualTo(human.getId());
        }

        @Test
        @DisplayName("GrantKeyword with TARGET scope is classified as beneficial")
        void grantKeywordTargetIsBeneficial() {
            Permanent ownCreature = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new EliteVanguard());

            // Opponent also has a creature — but beneficial should target own
            harness.addToBattlefield(human, new LlanowarElves());

            ActivatedAbility ability = new ActivatedAbility(false, null,
                    List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                    "Target creature gains flying until end of turn.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            assertThat(target).isEqualTo(ownCreature.getId());
        }

        @Test
        @DisplayName("Cost effects do not affect beneficial/harmful classification")
        void costEffectsDoNotAffectClassification() {
            // Sacrifice self cost + damage ability — should still be classified as harmful
            Permanent oppCreature = harness.addToBattlefieldAndReturn(human, new LlanowarElves());
            Permanent source = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());

            ActivatedAbility ability = new ActivatedAbility(false, null,
                    List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(1)),
                    "Sacrifice: deal 1 damage to any target.");

            UUID target = targetSelector.chooseAbilityTarget(gd, ability, aiPlayer.getId(), source);

            // Should target opponent's creature (harmful classification)
            assertThat(target).isEqualTo(oppCreature.getId());
        }
    }

    // ===== Contextual threat targeting =====

    @Nested
    @DisplayName("Contextual threat targeting (with BoardEvaluator)")
    class ContextualThreatTargeting {

        private AiTargetSelector threatAwareSelector;

        @BeforeEach
        void setUp() {
            BoardEvaluator boardEvaluator = new BoardEvaluator(harness.getGameQueryService());
            threatAwareSelector = new AiTargetSelector(
                    harness.getGameQueryService(), harness.getTargetValidationService(),
                    harness.getTargetLegalityService(), boardEvaluator);
        }

        @Test
        @DisplayName("Destroy spell targets lord over bigger vanilla creature")
        void destroySpellTargetsLordOverVanilla() {
            // Benalish Marshal: 3/3 lord giving +1/+1 to other creatures
            Permanent marshal = harness.addToBattlefieldAndReturn(human, new BenalishMarshal());
            // Add 5 creatures to be pumped by the lord — typical "wide board" scenario
            // With 5 creatures, lord bonus = 5 * 4.5 = 22.5, clearly exceeding flying threat
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            // Air Elemental: 4/4 flying — bigger body but no lord effect
            harness.addToBattlefield(human, new AirElemental());

            // Create a simple destroy-target-creature spell
            Card destroySpell = new Card();
            destroySpell.setName("Test Destroy");
            destroySpell.setType(CardType.INSTANT);
            destroySpell.addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

            UUID target = threatAwareSelector.chooseTarget(gd, destroySpell, aiPlayer.getId());

            // Should prefer the lord (smaller body but pumping 5 creatures)
            assertThat(target).isEqualTo(marshal.getId());
        }

        @Test
        @DisplayName("Destroy spell targets growth creature over vanilla when no lord present")
        void destroySpellTargetsGrowthCreatureOverVanilla() {
            // Bloodcrazed Neonate: 2/1 with "whenever deals combat damage, put a +1/+1 counter"
            Permanent neonate = harness.addToBattlefieldAndReturn(human, new BloodcrazedNeonate());
            // GrizzlyBears: 2/2 vanilla
            harness.addToBattlefield(human, new GrizzlyBears());

            Card destroySpell = new Card();
            destroySpell.setName("Test Destroy");
            destroySpell.setType(CardType.INSTANT);
            destroySpell.addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

            UUID target = threatAwareSelector.chooseTarget(gd, destroySpell, aiPlayer.getId());

            // Growth creature should be prioritized due to scaling threat
            assertThat(target).isEqualTo(neonate.getId());
        }

        @Test
        @DisplayName("Exile spell targets lord over bigger vanilla creature")
        void exileSpellTargetsLordOverVanilla() {
            // Same wide-board setup as the destroy test — exile should route through the
            // same removal-target selection and use contextual threat scoring.
            Permanent marshal = harness.addToBattlefieldAndReturn(human, new BenalishMarshal());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new AirElemental());

            Card exileSpell = new Card();
            exileSpell.setName("Test Exile");
            exileSpell.setType(CardType.INSTANT);
            exileSpell.addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());

            UUID target = threatAwareSelector.chooseTarget(gd, exileSpell, aiPlayer.getId());

            assertThat(target).isEqualTo(marshal.getId());
        }

        @Test
        @DisplayName("Bounce spell targets lord over bigger vanilla creature")
        void bounceSpellTargetsLordOverVanilla() {
            // Same wide-board setup — single-target bounce routes through the same
            // removal-target selection as destroy/exile, and the AI's own creature
            // must not distract it (bounce previously used the own-first fallback).
            harness.addToBattlefield(aiPlayer, new GrizzlyBears());
            Permanent marshal = harness.addToBattlefieldAndReturn(human, new BenalishMarshal());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new AirElemental());

            Card bounceSpell = new Card();
            bounceSpell.setName("Test Bounce");
            bounceSpell.setType(CardType.INSTANT);
            bounceSpell.addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

            UUID target = threatAwareSelector.chooseTarget(gd, bounceSpell, aiPlayer.getId());

            assertThat(target).isEqualTo(marshal.getId());
        }

        @Test
        @DisplayName("Damage spell (general fallback) targets lord over bigger vanilla creature")
        void damageSpellTargetsLordOverVanilla() {
            // Damage-to-any-target spells don't go through the destroy path — they fall into
            // the general fallback. Before the fix this just picked whichever creature came
            // first on the battlefield list; now it should pick the most threatening one.
            Permanent marshal = harness.addToBattlefieldAndReturn(human, new BenalishMarshal());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new AirElemental());

            Card damageSpell = new Card();
            damageSpell.setName("Test Bolt");
            damageSpell.setType(CardType.INSTANT);
            damageSpell.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

            UUID target = threatAwareSelector.chooseTarget(gd, damageSpell, aiPlayer.getId());

            assertThat(target).isEqualTo(marshal.getId());
        }

        @Test
        @DisplayName("Without BoardEvaluator, falls back to raw power for destroy target")
        void withoutBoardEvaluatorFallsBackToRawPower() {
            // Same setup as lord test, but using selector without BoardEvaluator
            harness.addToBattlefield(human, new BenalishMarshal());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            harness.addToBattlefield(human, new GrizzlyBears());
            Permanent airElemental = harness.addToBattlefieldAndReturn(human, new AirElemental());

            Card destroySpell = new Card();
            destroySpell.setName("Test Destroy");
            destroySpell.setType(CardType.INSTANT);
            destroySpell.addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

            // Original selector without BoardEvaluator — uses raw power
            UUID target = targetSelector.chooseTarget(gd, destroySpell, aiPlayer.getId());

            // Should pick Air Elemental (4 power > Marshal's 3 power)
            assertThat(target).isEqualTo(airElemental.getId());
        }
    }
}
