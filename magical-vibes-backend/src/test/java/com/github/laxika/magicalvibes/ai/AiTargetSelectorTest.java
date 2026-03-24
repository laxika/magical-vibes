package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WizardsLightning;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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
                harness.getGameQueryService(), harness.getTargetValidationService());
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
                        "ExileTargetCardFromGraveyard(CREATURE) filters to creatures only",
                        new ExileTargetCardFromGraveyardEffect(CardType.CREATURE),
                        Set.of("GY Creature")
                ),
                Arguments.of(
                        "ExileTargetCardFromGraveyard(null) allows all card types",
                        new ExileTargetCardFromGraveyardEffect(null),
                        Set.of("GY Creature", "GY Instant", "GY Sorcery", "GY Artifact", "GY Enchantment", "GY Basic Land")
                ),
                Arguments.of(
                        "GrantFlashbackToTargetGraveyardCard filters to matching card types",
                        new GrantFlashbackToTargetGraveyardCardEffect(Set.of(CardType.INSTANT, CardType.SORCERY)),
                        Set.of("GY Instant", "GY Sorcery")
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
}
