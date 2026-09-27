package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureSkywriterDjinnSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Skywriter Djinn's random spellbook and Domain hand-size repeat. */
@Component
public class ConjureSkywriterDjinnSpellbookEffectHandler implements NormalEffectHandlerBean {

    private static final List<String> SPELLBOOK_NAMES = List.of(
            "See the Truth", "Teferi's Time Twist", "Flood of Recollection", "Keep Safe",
            "Hard Evidence", "Ghostform", "Startle", "Hampering Snare", "Stifle",
            "Contentious Plan", "Majestic Metamorphosis", "Befuddle", "Bury in Books",
            "Choking Tethers", "Suit Up");

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    public ConjureSkywriterDjinnSpellbookEffectHandler(AmountEvaluationService amountEvaluationService,
                                                        GameLogService gameLogService) {
        this.amountEvaluationService = amountEvaluationService;
        this.gameLogService = gameLogService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureSkywriterDjinnSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }

        String cardName = SPELLBOOK_NAMES.get(ThreadLocalRandom.current().nextInt(SPELLBOOK_NAMES.size()));
        hand.add(createSpellbookCard(cardName, entry.getControllerId()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a random card from its spellbook into their hand."));

        int domain = amountEvaluationService.evaluate(gameData,
                new BasicLandTypesAmongControlledLands(), AmountContext.forStackEntry(entry, null));
        if (hand.size() < domain) {
            resolve(gameData, entry, effect);
        }
    }

    private Card createSpellbookCard(String name, UUID ownerId) {
        return switch (name) {
            case "See the Truth" -> seeTheTruth(ownerId);
            case "Teferi's Time Twist" -> teferisTimeTwist(ownerId);
            case "Flood of Recollection" -> floodOfRecollection(ownerId);
            case "Keep Safe" -> keepSafe(ownerId);
            case "Hard Evidence" -> hardEvidence(ownerId);
            case "Ghostform" -> ghostform(ownerId);
            case "Startle" -> startle(ownerId);
            case "Hampering Snare" -> hamperingSnare(ownerId);
            case "Stifle" -> stifle(ownerId);
            case "Contentious Plan" -> contentiousPlan(ownerId);
            case "Majestic Metamorphosis" -> majesticMetamorphosis(ownerId);
            case "Befuddle" -> befuddle(ownerId);
            case "Bury in Books" -> buryInBooks(ownerId);
            case "Choking Tethers" -> chokingTethers(ownerId);
            case "Suit Up" -> suitUp(ownerId);
            default -> throw new IllegalStateException("Unknown Skywriter Djinn spellbook card: " + name);
        };
    }

    private Card seeTheTruth(UUID ownerId) {
        Card card = spellCard(ownerId, "See the Truth", CardType.SORCERY, "{1}{U}",
                "Look at the top three cards of your library. Put one of those cards into your hand and the rest on the bottom of your library in any order. If this spell was cast from anywhere other than your hand, put each of those cards into your hand instead.");
        card.addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastFromZone(Zone.HAND),
                new LookAtTopCardsHandTopBottomEffect(3)));
        card.addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastNotFromHand(),
                new LookAtTopCardsEffect(new Fixed(3), new Fixed(3), null,
                        LookDestination.BOTTOM_OF_LIBRARY, false)));
        return freeze(card);
    }

    private Card teferisTimeTwist(UUID ownerId) {
        Card card = spellCard(ownerId, "Teferi's Time Twist", CardType.INSTANT, "{1}{U}",
                "Exile target permanent you control. Return that card to the battlefield under its owner's control at the beginning of the next end step. If it enters as a creature, it enters with an additional +1/+1 counter on it.");
        card.target(TargetFilters.permanentYouControl())
                .addEffect(EffectSlot.SPELL, FlickerEffect.exileTargetReturnAtEndStepWithCreatureCounters(1));
        return freeze(card);
    }

    private Card floodOfRecollection(UUID ownerId) {
        Card card = spellCard(ownerId, "Flood of Recollection", CardType.SORCERY, "{U}{U}",
                "Return target instant or sorcery card from your graveyard to your hand. Exile Flood of Recollection.");
        card.addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))))
                .targetGraveyard(true)
                .build());
        card.addEffect(EffectSlot.SPELL, new ExileSpellEffect());
        return freeze(card);
    }

    private Card keepSafe(UUID ownerId) {
        Card card = spellCard(ownerId, "Keep Safe", CardType.INSTANT, "{1}{U}",
                "Counter target spell that targets a permanent you control. Draw a card.");
        card.target(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsYourPermanentPredicate(),
                "Target spell must target a permanent you control."))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
        return freeze(card);
    }

    private Card hardEvidence(UUID ownerId) {
        Card card = spellCard(ownerId, "Hard Evidence", CardType.SORCERY, "{U}",
                "Create a 0/3 blue Crab creature token. Investigate.");
        card.addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 1, "Crab", 0, 3, CardColor.BLUE, Set.of(CardColor.BLUE),
                List.of(CardSubtype.CRAB), Set.of(), Set.of(), false, false, Map.of(), List.of(),
                false, false, false, 0, Set.of()));
        card.addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1));
        return freeze(card);
    }

    private Card ghostform(UUID ownerId) {
        Card card = spellCard(ownerId, "Ghostform", CardType.SORCERY, "{1}{U}",
                "Up to two target creatures can't be blocked this turn.");
        card.target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect());
        return freeze(card);
    }

    private Card startle(UUID ownerId) {
        Card card = spellCard(ownerId, "Startle", CardType.INSTANT, "{1}{U}",
                "Target creature gets -2/-0 until end of turn. Create a 2/2 black Zombie creature token with decayed. Draw a card.");
        card.target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-2, 0));
        card.addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombieWithDecayed(1));
        card.addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        return freeze(card);
    }

    private Card hamperingSnare(UUID ownerId) {
        Card card = spellCard(ownerId, "Hampering Snare", CardType.INSTANT, "{1}{U}",
                "Creatures your opponents control get -2/-0 until end of turn. Cycling {2}");
        card.addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, 0,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        card.addCycling("{2}");
        return freeze(card);
    }

    private Card stifle(UUID ownerId) {
        Card card = spellCard(ownerId, "Stifle", CardType.INSTANT, "{U}",
                "Counter target activated or triggered ability. (Mana abilities can't be targeted.)");
        card.target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.ACTIVATED_ABILITY, StackEntryType.TRIGGERED_ABILITY)),
                "Target must be an activated or triggered ability."))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        return freeze(card);
    }

    private Card contentiousPlan(UUID ownerId) {
        Card card = spellCard(ownerId, "Contentious Plan", CardType.SORCERY, "{1}{U}",
                "Proliferate. Draw a card.");
        card.addEffect(EffectSlot.SPELL, new ProliferateEffect());
        card.addEffect(EffectSlot.SPELL, new DrawCardEffect());
        return freeze(card);
    }

    private Card majesticMetamorphosis(UUID ownerId) {
        Card card = spellCard(ownerId, "Majestic Metamorphosis", CardType.INSTANT, "{2}{U}",
                "Until end of turn, target artifact or creature becomes a 4/4 Angel artifact creature and gains flying. Draw a card.");
        card.target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())),
                "Target must be an artifact or creature"))
                .addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                        4, 4, List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING),
                        null, Set.of(CardType.ARTIFACT), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
        return freeze(card);
    }

    private Card befuddle(UUID ownerId) {
        Card card = spellCard(ownerId, "Befuddle", CardType.INSTANT, "{2}{U}",
                "Target creature gets -4/-0 until end of turn. Draw a card.");
        card.addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, 0));
        card.addEffect(EffectSlot.SPELL, new DrawCardEffect());
        return freeze(card);
    }

    private Card buryInBooks(UUID ownerId) {
        Card card = spellCard(ownerId, "Bury in Books", CardType.INSTANT, "{4}{U}",
                "This spell costs {2} less to cast if it targets an attacking creature. Put target creature into its owner's library second from the top.");
        PermanentPredicate attackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentIsAttackingPredicate()));
        card.addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingPermanentEffect(attackingCreature, 2));
        card.target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                "Target must be a creature"))
                .addEffect(EffectSlot.SPELL, new PutTargetPermanentIntoLibraryNFromTopEffect(1));
        return freeze(card);
    }

    private Card chokingTethers(UUID ownerId) {
        Card card = spellCard(ownerId, "Choking Tethers", CardType.INSTANT, "{3}{U}",
                "Tap up to four target creatures. Cycling {1}{U}");
        card.target(TargetFilters.creature(), 0, 4)
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));
        card.addCycling("{1}{U}");
        card.addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                new TapPermanentsEffect(TapUntapScope.TARGET), "Tap target creature?"));
        return freeze(card);
    }

    private Card suitUp(UUID ownerId) {
        Card card = spellCard(ownerId, "Suit Up", CardType.INSTANT, "{2}{U}",
                "Until end of turn, target creature or Vehicle becomes an artifact creature with base power and toughness 4/5. Draw a card.");
        card.target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                "Target must be a creature or Vehicle"))
                .addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                        4, 5, List.of(), Set.of(), null, Set.of(CardType.ARTIFACT),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
        return freeze(card);
    }

    private Card spellCard(UUID ownerId, String name, CardType type, String manaCost, String cardText) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE));
        card.setCardText(cardText);
        card.setOwnerId(ownerId);
        card.setToken(true);
        card.setTokenCard(true);
        return card;
    }

    private Card freeze(Card card) {
        card.freeze();
        return card;
    }
}
