package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceOrPairedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The named factory behind every {@link PermanentSubject}, {@link PlayerSubject} and {@link CardSubject}, so an effect reads like its card
 * text: {@code controllerOf(triggering())} is "that creature's controller". The variants are listed in
 * {@code agent-docs/SUBJECTS_REFERENCE.md}.
 */
public final class Subjects {

    private static final PermanentSubject SOURCE = new PermanentSubject.Source();
    private static final PermanentSubject TARGET = new PermanentSubject.Target(null);
    private static final PermanentSubject TARGET_DECLARED_ELSEWHERE = new PermanentSubject.TargetDeclaredElsewhere();
    private static final PermanentSubject ALL_TARGETS = new PermanentSubject.AllTargets();
    private static final PermanentSubject ATTACHED = new PermanentSubject.Attached();
    private static final PermanentSubject ATTACHED_CREATURE = new PermanentSubject.Attached(true);
    private static final PermanentSubject ATTACHED_OR_SOURCE = new PermanentSubject.AttachedOrSource();
    private static final PermanentSubject ATTACHED_AT_TRIGGER = new PermanentSubject.AttachedAtTrigger();
    private static final PermanentSubject TRIGGERING = new PermanentSubject.Triggering();
    private static final PermanentSubject TRIGGERING_ALL = new PermanentSubject.TriggeringAll();
    private static final PermanentSubject TRIGGERING_OR_SOURCE = new PermanentSubject.TriggeringOrSource();
    private static final PermanentSubject TRIGGERING_CARD = new PermanentSubject.TriggeringCard();
    private static final PermanentSubject TARGET_SPELL = new PermanentSubject.TargetSpell();
    private static final PermanentSubject TARGET_ABILITY_SOURCE = new PermanentSubject.TargetAbilitySource(null);
    private static final PermanentSubject TRIGGERING_SPELL = new PermanentSubject.TriggeringSpell();
    private static final PermanentSubject RETURNED = new PermanentSubject.Returned();
    private static final PermanentSubject GRANTER = new PermanentSubject.Granter(null);
    private static final PermanentSubject RETURNED_THIS_RESOLUTION = new PermanentSubject.ReturnedThisResolution();
    private static final PermanentSubject EXILED_THIS_RESOLUTION = new PermanentSubject.ExiledThisResolution();
    private static final PermanentSubject TRIGGERING_ATTACKERS = new PermanentSubject.TriggeringAttackers();
    private static final PermanentSubject TRIGGERING_SPELL_TARGETS = new PermanentSubject.TriggeringSpellTargets();
    private static final PermanentSubject CREATED_THIS_RESOLUTION = new PermanentSubject.CreatedThisResolution();
    private static final PermanentSubject SEARCHED = new PermanentSubject.Searched();
    private static final PermanentSubject ATTACHED_AND_SHARING_CREATURE_TYPE =
            new PermanentSubject.AttachedAndSharingCreatureType();

    private static final PlayerSubject YOU = new PlayerSubject.You();
    private static final PlayerSubject TARGET_PLAYER = new PlayerSubject.TargetPlayer(null, true);
    private static final PlayerSubject TARGET_PLAYER_DECLARED_ELSEWHERE = new PlayerSubject.TargetPlayer(null, false);
    private static final PlayerSubject EACH_PLAYER = new PlayerSubject.Each(PlayerSubject.Relation.ALL);
    private static final PlayerSubject EACH_OPPONENT = new PlayerSubject.Each(PlayerSubject.Relation.OPPONENTS);
    private static final PlayerSubject TRIGGERING_PLAYER = new PlayerSubject.TriggeringPlayer();
    private static final PlayerSubject ACTIVE_PLAYER = new PlayerSubject.ActivePlayer();
    private static final PlayerSubject DEFENDING_PLAYER = new PlayerSubject.DefendingPlayer();
    private static final PlayerSubject ENCHANTED_PLAYER = new PlayerSubject.EnchantedPlayer();
    private static final PlayerSubject CHOSEN_PLAYER = new PlayerSubject.ChosenPlayer();
    private static final PlayerSubject CHOSEN_PLAYERS = new PlayerSubject.ChosenPlayers();
    private static final PlayerSubject ACCEPTED_PLAYERS = new PlayerSubject.AcceptedPlayers();
    private static final PlayerSubject PLAYERS_DEALT_DAMAGE = new PlayerSubject.PlayersDealtDamage();
    private static final PlayerSubject SUBGAME_WINNERS = new PlayerSubject.SubgameWinners();

    private static final PermanentPredicate CREATURE = new PermanentIsCreaturePredicate();
    private static final PermanentPredicate LAND = new PermanentIsLandPredicate();
    private static final PermanentPredicate NOT_SOURCE =
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate());
    private static final PermanentPredicate TAPPED = new PermanentIsTappedPredicate();

    private static final PermanentSubject SOURCE_AND_PAIRED =
            new PermanentSubject.ControlledBy(EACH_PLAYER, new PermanentIsSourceOrPairedPredicate());
    private static final PermanentSubject CREATURES_YOU_CONTROL = new PermanentSubject.ControlledBy(YOU, CREATURE);
    private static final PermanentSubject OTHER_CREATURES_YOU_CONTROL =
            new PermanentSubject.ControlledBy(YOU, allOfPredicates(NOT_SOURCE, CREATURE));
    private static final PermanentSubject OTHER_TAPPED_CREATURES_YOU_CONTROL =
            new PermanentSubject.ControlledBy(YOU, allOfPredicates(TAPPED, NOT_SOURCE, CREATURE));
    private static final PermanentSubject OTHER_UNTAPPED_CREATURES_YOU_CONTROL =
            new PermanentSubject.ControlledBy(YOU,
                    allOfPredicates(new PermanentNotPredicate(TAPPED), NOT_SOURCE, CREATURE));
    private static final PermanentSubject CREATURES_YOUR_OPPONENTS_CONTROL =
            new PermanentSubject.ControlledBy(EACH_OPPONENT, CREATURE);
    private static final PermanentSubject PERMANENTS_YOU_CONTROL =
            new PermanentSubject.ControlledBy(YOU, new PermanentTruePredicate());
    private static final PermanentSubject OTHER_PERMANENTS_YOU_CONTROL =
            new PermanentSubject.ControlledBy(YOU, NOT_SOURCE);
    private static final PermanentSubject CREATURES_TARGET_PLAYER_CONTROLS =
            new PermanentSubject.ControlledBy(TARGET_PLAYER, CREATURE);
    private static final PermanentSubject LANDS_TARGET_PLAYER_CONTROLS =
            new PermanentSubject.ControlledBy(TARGET_PLAYER, LAND);
    private static final PermanentSubject ALL_CREATURES = new PermanentSubject.ControlledBy(EACH_PLAYER, CREATURE);
    private static final PermanentSubject OTHER_CREATURES =
            new PermanentSubject.ControlledBy(EACH_PLAYER, allOfPredicates(NOT_SOURCE, CREATURE));
    private static final PermanentSubject ALL_PERMANENTS =
            new PermanentSubject.ControlledBy(EACH_PLAYER, new PermanentTruePredicate());
    private static final PermanentSubject OTHER_PERMANENTS = new PermanentSubject.ControlledBy(EACH_PLAYER, NOT_SOURCE);
    private static final PermanentSubject CREATURES_ENCHANTED_PLAYER_CONTROLS =
            new PermanentSubject.ControlledBy(ENCHANTED_PLAYER, CREATURE);
    private static final PermanentSubject LANDS_YOU_CONTROL = new PermanentSubject.ControlledBy(YOU, LAND);
    private static final PermanentSubject LANDS_YOUR_OPPONENTS_CONTROL =
            new PermanentSubject.ControlledBy(EACH_OPPONENT, LAND);
    private static final PermanentSubject ALL_LANDS = new PermanentSubject.ControlledBy(EACH_PLAYER, LAND);
    private static final PermanentSubject OTHER_LANDS =
            new PermanentSubject.ControlledBy(EACH_PLAYER, allOfPredicates(NOT_SOURCE, LAND));

    private Subjects() {
    }

    /** "This permanent". */
    public static PermanentSubject source() {
        return SOURCE;
    }

    /** "Target permanent": the targets the stack entry assigns to the effect. */
    public static PermanentSubject target() {
        return TARGET;
    }

    /** The targets chosen for target group {@code group}. */
    public static PermanentSubject target(int group) {
        return new PermanentSubject.Target(group);
    }

    /** Every permanent target of the stack entry, across all its target groups ("untap up to four target permanents"). */
    public static PermanentSubject allTargets() {
        return ALL_TARGETS;
    }

    /** "Enchanted permanent": what the source Aura or Equipment is attached to, whatever it is. */
    public static PermanentSubject attached() {
        return ATTACHED;
    }

    /**
     * "Enchanted creature", "equipped creature": what the source Aura or Equipment is attached to, which a resolving
     * effect names only while it is a creature.
     */
    public static PermanentSubject attachedCreature() {
        return ATTACHED_CREATURE;
    }

    /** "This creature ... If it's an Aura, enchanted creature ... instead": the host while attached, else the source. */
    public static PermanentSubject attachedOrSource() {
        return ATTACHED_OR_SOURCE;
    }

    /**
     * "That creature" naming the host from the trigger condition: the permanent the source was attached to when the
     * ability triggered or was activated.
     */
    public static PermanentSubject attachedAtTrigger() {
        return ATTACHED_AT_TRIGGER;
    }

    /** "It", "that creature": the permanent the trigger event was about. */
    public static PermanentSubject triggering() {
        return TRIGGERING;
    }

    /** "Them", "each of them": every permanent the trigger event was about (the group of a "one or more" event). */
    public static PermanentSubject triggeringAll() {
        return TRIGGERING_ALL;
    }

    /** "It" after "whenever this creature or equipped creature attacks": the recorded permanent, else the source. */
    public static PermanentSubject triggeringOrSource() {
        return TRIGGERING_OR_SOURCE;
    }

    /**
     * "That card": the spell cast or land played the trigger was about; with {@link #ownerOf}, also the card a
     * graveyard or zone-change event was about, or the permanent that entered, as it is or last was.
     */
    public static PermanentSubject triggeringCard() {
        return TRIGGERING_CARD;
    }

    /** "Target spell", for {@link #controllerOf}: "that spell's controller". Declares nothing by itself. */
    public static PermanentSubject targetSpell() {
        return TARGET_SPELL;
    }

    /**
     * "That permanent" after "counter target activated or triggered ability": the permanent the targeted ability came
     * from, on the stack or as the counter step recorded it. Declares nothing by itself.
     */
    public static PermanentSubject targetAbilitySource() {
        return TARGET_ABILITY_SOURCE;
    }

    /** {@link #targetAbilitySource()} while that permanent matches {@code filter} as the reading effect is applied. */
    public static PermanentSubject targetAbilitySource(PermanentPredicate filter) {
        return new PermanentSubject.TargetAbilitySource(filter);
    }

    /** "That spell or ability", for {@link #controllerOf}: the spell or ability the trigger event was about. */
    public static PermanentSubject triggeringSpell() {
        return TRIGGERING_SPELL;
    }

    /** The permanent a preceding "return target card from a graveyard to the battlefield" put onto the battlefield. */
    public static PermanentSubject returned() {
        return RETURNED;
    }

    /** Every permanent an earlier step of the same resolution returned to the battlefield ("exile them again"). */
    public static PermanentSubject returnedThisResolution() {
        return RETURNED_THIS_RESOLUTION;
    }

    /** The card an earlier step of the same resolution exiled from the effect's target ("return that card"). */
    public static PermanentSubject exiledThisResolution() {
        return EXILED_THIS_RESOLUTION;
    }

    /** "They", "those creatures" in a "whenever you attack" ability: the attackers the trigger event recorded. */
    public static PermanentSubject triggeringAttackers() {
        return TRIGGERING_ATTACKERS;
    }

    /**
     * "Those creatures": the targets of the spell the trigger event was about, else, with no triggering spell, the
     * effect's own targets (declared by the card, not by this subject).
     */
    public static PermanentSubject triggeringSpellTargets() {
        return TRIGGERING_SPELL_TARGETS;
    }

    /** "Those tokens": the permanents an earlier step of the same resolution created. */
    public static PermanentSubject createdThisResolution() {
        return CREATED_THIS_RESOLUTION;
    }

    /** "It", "that land": the permanents a search of the same resolution put onto the battlefield. */
    public static PermanentSubject searched() {
        return SEARCHED;
    }

    /** "Enchanted creature and other creatures that share a creature type with it" (the Crowns). */
    public static PermanentSubject attachedAndSharingCreatureType() {
        return ATTACHED_AND_SHARING_CREATURE_TYPE;
    }

    /**
     * "Those creatures": the permanents the most recent {@code kind} step of the same resolution acted on, still on the
     * battlefield ({@link StepKind#PUT_COUNTERS}, {@link StepKind#REMOVE_COUNTERS}, {@link StepKind#DEAL_DAMAGE} only;
     * see {@link PermanentSubject.ThisWay}). A step result.
     */
    public static PermanentSubject thisWay(StepKind kind) {
        return new PermanentSubject.ThisWay(kind);
    }

    /**
     * "If a Dragon is dealt damage this way, destroy it": the permanents {@link #thisWay(StepKind)} names that match
     * {@code filter} as the reading effect is applied.
     */
    public static PermanentSubject thisWay(StepKind kind, PermanentPredicate filter) {
        return new PermanentSubject.ThisWay(kind, filter);
    }

    /**
     * "Each nontoken artifact destroyed this way", "a duplicate of it" after a return to a hand: the permanents the most
     * recent {@code kind} step moved off the battlefield, as they last existed there (see
     * {@link PermanentSubject.DepartedThisWay}). A step result.
     */
    public static PermanentSubject departedThisWay(StepKind kind) {
        return new PermanentSubject.DepartedThisWay(kind);
    }

    /**
     * "That permanent" naming the target another effect of the same ability declares, read without declaring one (see
     * {@link PermanentSubject.TargetDeclaredElsewhere}).
     */
    public static PermanentSubject targetDeclaredElsewhere() {
        return TARGET_DECLARED_ELSEWHERE;
    }

    /**
     * "Each permanent with the most votes or tied for most votes": the permanents the resolution's last vote named most
     * (see {@link PermanentSubject.Voted}). A step result.
     */
    public static PermanentSubject mostVoted() {
        return new PermanentSubject.Voted(VoteEffect.Tally.MOST);
    }

    /**
     * "Each creature with one or more votes": the permanents the resolution's last vote named (see
     * {@link PermanentSubject.Voted}). A step result.
     */
    public static PermanentSubject votedFor() {
        return new PermanentSubject.Voted(VoteEffect.Tally.ANY);
    }

    /**
     * The permanents {@code permanents} names but those {@code excluded} names ("then exile all other Nightmare tokens
     * you control" after the copy step: {@code except(controlledBy(you(), filter), createdThisResolution())}).
     */
    public static PermanentSubject except(PermanentSubject permanents, PermanentSubject excluded) {
        return new PermanentSubject.Except(permanents, excluded);
    }

    /**
     * "The exiled card", "each card put into a graveyard this way": the cards the most recent step of that kind of the
     * same resolution moved, in the zone it put them in ({@code EXILE}, {@code DESTROY}, {@code SACRIFICE},
     * {@code DISCARD}: "the discarded card"; {@code COUNTER_SPELL}: "that card" after "counter target spell … exile it
     * instead"). A step result.
     */
    public static CardSubject cardsThisWay(StepKind kind) {
        return new CardSubject.CardsThisWay(kind);
    }

    /**
     * {@link #cardsThisWay(StepKind)} narrowed to the cards matching {@code filter} as they are now: "that card" after
     * "exile cards from the top of your library until you exile a nonland card" is
     * {@code cardsThisWay(EXILE, nonland)}. A step result.
     */
    public static CardSubject cardsThisWay(StepKind kind, CardPredicate filter) {
        return new CardSubject.CardsThisWay(kind, filter);
    }

    /**
     * "Each card with the most votes or tied for most votes": the cards the resolution's last vote named most, still
     * where they were voted for (see {@link CardSubject.Voted}). A step result.
     */
    public static CardSubject mostVotedCards() {
        return new CardSubject.Voted(VoteEffect.Tally.MOST);
    }

    /**
     * "That card", "it" after "search your library for a card and [put it into your hand | put it into your graveyard |
     * exile it | reveal it]": the cards the resolution's most recent library search found, still in the zone the search
     * put them in (the library, for a search that reveals and keeps them there). A step result.
     */
    public static CardSubject foundThisWay() {
        return cardsThisWay(StepKind.SEARCH);
    }

    /** "All cards revealed this way": those still where the last reveal step showed them. */
    public static CardSubject revealedThisWay() {
        return cardsThisWay(StepKind.REVEAL);
    }

    /** "That card", "the creature cards revealed this way": of those, the ones matching {@code filter}. */
    public static CardSubject revealedThisWay(CardPredicate filter) {
        return cardsThisWay(StepKind.REVEAL, filter);
    }

    /** "The rest" after "put that card ...": the revealed cards that don't match {@code filter}. */
    public static CardSubject restRevealedThisWay(CardPredicate filter) {
        return except(revealedThisWay(), revealedThisWay(filter));
    }

    /**
     * "One of them": the cards the trigger event was about that match {@code filter}, each still in the graveyard stay
     * the event put it in.
     */
    public static CardSubject triggeringCards(CardPredicate filter) {
        return new CardSubject.TriggeringCards(filter);
    }

    /** "That card", "it": {@link #triggeringCards(CardPredicate)} without a filter. */
    public static CardSubject triggeringCards() {
        return new CardSubject.TriggeringCards(null);
    }

    /**
     * "That card", "those cards" after "whenever a card is put into your hand from [zone]" or "whenever you seek one or
     * more cards": the event's cards in the hand they were put into.
     */
    public static CardSubject triggeringCardsInHand() {
        return new CardSubject.TriggeringCardsInHand();
    }

    /**
     * "The exiled card" after "exile a card from your graveyard" as a cost, "the exiled cards" after "exile two creature
     * cards from your graveyard": {@link CardSubject.ExiledAsCost}.
     */
    public static CardSubject exiledAsCost() {
        return new CardSubject.ExiledAsCost();
    }

    /** "This card" in an ability that works from a graveyard. */
    public static CardSubject sourceCard() {
        return new CardSubject.SourceCard();
    }

    /** "A [filter] card from your graveyard", chosen or acted on without a target. */
    public static CardSubject cardsInYourGraveyard(CardPredicate filter) {
        return new CardSubject.InGraveyards(you(), filter);
    }

    /** The cards matching {@code filter} in the graveyards of the players {@code owners} names. */
    public static CardSubject cardsInGraveyards(PlayerSubject owners, CardPredicate filter) {
        return new CardSubject.InGraveyards(owners, filter);
    }

    /** "An exiled [filter] card", whoever owns it. */
    public static CardSubject exiledCards(CardPredicate filter) {
        return new CardSubject.InExile(null, filter);
    }

    /** "A [filter] card you own in exile": the exiled cards the players {@code owners} names own. */
    public static CardSubject exiledCards(PlayerSubject owners, CardPredicate filter) {
        return new CardSubject.InExile(owners, filter);
    }

    /** "The cards exiled with this", "the exiled card" of a linked ability. */
    public static CardSubject exiledWithSource() {
        return new CardSubject.ExiledWithSource(null, null);
    }

    /**
     * "A [filter] card exiled with this": of {@link #exiledWithSource()}, those {@code filter} matches; a card exiled face
     * down has no characteristics to match (CR 406.3a).
     */
    public static CardSubject exiledWithSourceMatching(CardPredicate filter) {
        return new CardSubject.ExiledWithSource(null, filter);
    }

    /** "The cards you own exiled with this": of {@link #exiledWithSource()}, those {@code owners} own. */
    public static CardSubject exiledWithSource(PlayerSubject owners) {
        return new CardSubject.ExiledWithSource(owners, null);
    }

    /** "The rest": those of {@code cards} that {@code excluded} doesn't name. */
    public static CardSubject except(CardSubject cards, CardSubject excluded) {
        return new CardSubject.Except(cards, excluded);
    }

    /** "The card you discarded": those of {@code cards} that a player {@code owners} names owns, wherever they are. */
    public static CardSubject ownedBy(CardSubject cards, PlayerSubject owners) {
        return new CardSubject.OwnedBy(cards, owners);
    }

    /** "[Cards] in your graveyard": those of {@code cards} in the controller's graveyard. */
    public static CardSubject inYourGraveyard(CardSubject cards) {
        return new CardSubject.InYourGraveyard(cards);
    }

    /** "Target face-up exiled card", "target exiled card with warp": a card in exile the effect targets. */
    public static CardSubject targetExiledCard(CardPredicate filter) {
        return new CardSubject.TargetExiledCard(filter, false,
                com.github.laxika.magicalvibes.model.filter.PlayerRelation.ANY);
    }

    /** "Target card you don't own in exile": a target card in exile of the owner {@code owner} names. */
    public static CardSubject targetExiledCard(CardPredicate filter,
                                               com.github.laxika.magicalvibes.model.filter.PlayerRelation owner) {
        return new CardSubject.TargetExiledCard(filter, false, owner);
    }

    /** "Target [filter] card exiled with this creature". */
    public static CardSubject targetCardExiledWithSource(CardPredicate filter) {
        return new CardSubject.TargetExiledCard(filter, true,
                com.github.laxika.magicalvibes.model.filter.PlayerRelation.ANY);
    }

    /** "The top card of [owners'] graveyard", if it matches {@code filter}. */
    public static CardSubject topOfGraveyard(PlayerSubject owners, CardPredicate filter) {
        return new CardSubject.TopOfGraveyard(owners, filter);
    }

    /** "A [filter] card from your hand". */
    public static CardSubject cardsInYourHand(CardPredicate filter) {
        return new CardSubject.InHand(you(), filter);
    }

    /** The cards matching {@code filter} in the hands of the players {@code owners} names. */
    public static CardSubject cardsInHands(PlayerSubject owners, CardPredicate filter) {
        return new CardSubject.InHand(owners, filter);
    }

    /** The cards matching {@code filter} in the controller's library: only for a choice at random. */
    public static CardSubject cardsInYourLibrary(CardPredicate filter) {
        return new CardSubject.InLibrary(you(), filter);
    }

    /**
     * "A random [filter] card from their library": the cards matching {@code filter} in the libraries of the players
     * {@code owners} names, only for a choice at random.
     */
    public static CardSubject cardsInLibraries(PlayerSubject owners, CardPredicate filter) {
        return new CardSubject.InLibrary(owners, filter);
    }

    /** "The topmost creature card in your library": the matching card nearest the top of your library. */
    public static CardSubject topmostCardInYourLibrary(CardPredicate filter) {
        return new CardSubject.TopmostInLibrary(you(), filter);
    }

    /**
     * "All [filter] cards you own" (digital only): those cards in every zone, the battlefield and the stack included;
     * only for the perpetual verbs.
     */
    public static CardSubject cardsYouOwn(CardPredicate filter) {
        return new CardSubject.Owned(you(), filter);
    }

    /** "From the command zone": the cards matching {@code filter} in the controller's command zone. */
    public static CardSubject cardsInYourCommandZone(CardPredicate filter) {
        return new CardSubject.InCommandZone(you(), filter);
    }

    /**
     * "Target [filter] card in your graveyard / in a graveyard": the effect's card target in a graveyard of
     * {@code scope}; the effect reading it declares the target.
     */
    public static CardSubject targetCard(CardPredicate filter, GraveyardSearchScope scope) {
        return new CardSubject.TargetCard(filter, scope);
    }

    /** "Each other creature banded with it": the other attacking creatures in {@code permanent}'s band. */
    public static PermanentSubject bandmatesOf(PermanentSubject permanent) {
        return new PermanentSubject.BandmatesOf(permanent);
    }

    /** "Target creature and each other creature that shares a color with it". */
    public static PermanentSubject withCreaturesSharingAColor(PermanentSubject permanent) {
        return new PermanentSubject.WithCreaturesSharingAColor(permanent);
    }

    /**
     * "Each other creature that shares a color with it" (and a name, a creature type): the creatures other than the one
     * {@code permanent} names that share {@code quality} with it as the effect resolves; nothing once the named
     * permanent is no longer a creature. Declares no target.
     */
    public static PermanentSubject otherCreaturesSharing(PermanentSubject permanent,
                                                         PermanentSubject.OtherPermanentsSharing.SharedQuality quality) {
        return new PermanentSubject.OtherPermanentsSharing(permanent, quality, CREATURE);
    }

    /**
     * "All other permanents with the same name as that permanent", "each other nonland permanent with the same mana
     * value as that permanent": the permanents matching {@code filter}, other than the one {@code permanent} names,
     * that share {@code quality} with it as the effect resolves; nothing once the named permanent no longer matches
     * {@code filter}. Declares no target.
     */
    public static PermanentSubject otherPermanentsSharing(PermanentSubject permanent,
                                                          PermanentSubject.OtherPermanentsSharing.SharedQuality quality,
                                                          PermanentPredicate filter) {
        return new PermanentSubject.OtherPermanentsSharing(permanent, quality, filter);
    }

    /**
     * "All other creatures that player controls with the same name as that creature", "all tokens that player controls
     * with the same name as that permanent": the others matching {@code filter} of the controller of the permanent
     * {@code permanent} names that share {@code quality} with it.
     */
    public static PermanentSubject othersItsControllerControlsSharing(
            PermanentSubject permanent, PermanentSubject.OtherPermanentsSharing.SharedQuality quality,
            PermanentPredicate filter) {
        return new PermanentSubject.OtherPermanentsSharing(permanent, quality, filter, true);
    }

    /**
     * "This creature and the creature it's paired with" (soulbond, CR 702.95): the source and its pair, judged as a
     * set.
     */
    public static PermanentSubject sourceAndPaired() {
        return SOURCE_AND_PAIRED;
    }

    /** "Creatures you control", the source included. */
    public static PermanentSubject creaturesYouControl() {
        return CREATURES_YOU_CONTROL;
    }

    /** "Other creatures you control". */
    public static PermanentSubject otherCreaturesYouControl() {
        return OTHER_CREATURES_YOU_CONTROL;
    }

    /** "Other tapped creatures you control". */
    public static PermanentSubject otherTappedCreaturesYouControl() {
        return OTHER_TAPPED_CREATURES_YOU_CONTROL;
    }

    /** "Other untapped creatures you control". */
    public static PermanentSubject otherUntappedCreaturesYouControl() {
        return OTHER_UNTAPPED_CREATURES_YOU_CONTROL;
    }

    /** "Creatures your opponents control". */
    public static PermanentSubject creaturesYourOpponentsControl() {
        return CREATURES_YOUR_OPPONENTS_CONTROL;
    }

    /** "Permanents you control", the source included. */
    public static PermanentSubject permanentsYouControl() {
        return PERMANENTS_YOU_CONTROL;
    }

    /** "Other permanents you control". */
    public static PermanentSubject otherPermanentsYouControl() {
        return OTHER_PERMANENTS_YOU_CONTROL;
    }

    /** "Creatures target player controls"; declares the player target. */
    public static PermanentSubject creaturesTargetPlayerControls() {
        return CREATURES_TARGET_PLAYER_CONTROLS;
    }

    /** "Lands target player controls"; declares the player target. */
    public static PermanentSubject landsTargetPlayerControls() {
        return LANDS_TARGET_PLAYER_CONTROLS;
    }

    /** "All creatures", the source included. */
    public static PermanentSubject allCreatures() {
        return ALL_CREATURES;
    }

    /** "All other creatures", every player's. */
    public static PermanentSubject otherCreatures() {
        return OTHER_CREATURES;
    }

    /** "All permanents", the source included. */
    public static PermanentSubject allPermanents() {
        return ALL_PERMANENTS;
    }

    /** "All other permanents", every player's. */
    public static PermanentSubject otherPermanents() {
        return OTHER_PERMANENTS;
    }

    /** "Creatures enchanted player controls". */
    public static PermanentSubject creaturesEnchantedPlayerControls() {
        return CREATURES_ENCHANTED_PLAYER_CONTROLS;
    }

    /** "Lands you control". */
    public static PermanentSubject landsYouControl() {
        return LANDS_YOU_CONTROL;
    }

    /** "Lands your opponents control". */
    public static PermanentSubject landsYourOpponentsControl() {
        return LANDS_YOUR_OPPONENTS_CONTROL;
    }

    /** "All lands", the source included. */
    public static PermanentSubject allLands() {
        return ALL_LANDS;
    }

    /** "All other lands", every player's. */
    public static PermanentSubject otherLands() {
        return OTHER_LANDS;
    }

    /** "This Equipment" in an ability it grants: the permanent that granted the resolving ability. */
    public static PermanentSubject granter() {
        return GRANTER;
    }

    /** {@link #granter()} bound to the granting permanent an activated ability recorded as it was activated. */
    public static PermanentSubject granter(UUID grantingPermanentId) {
        return new PermanentSubject.Granter(grantingPermanentId);
    }

    /** "That creature" bound by the effect that created a delayed ability: the permanent {@code permanentId}. */
    public static PermanentSubject named(UUID permanentId) {
        return new PermanentSubject.Named(permanentId);
    }

    /** Every permanent matching {@code filter} that {@code players} control. */
    public static PermanentSubject controlledBy(PlayerSubject players, PermanentPredicate filter) {
        return new PermanentSubject.ControlledBy(players, filter);
    }

    /** Every permanent {@code players} control. */
    public static PermanentSubject controlledBy(PlayerSubject players) {
        return new PermanentSubject.ControlledBy(players, new PermanentTruePredicate());
    }

    /**
     * The set {@code set} names, narrowed to the permanents that also match {@code filter} ("creatures you control" with
     * an effect's own filter); a {@code null} filter leaves it as it is.
     *
     * @throws IllegalArgumentException if {@code set} is not a set ({@link PermanentSubject#isSet()})
     */
    public static PermanentSubject narrowed(PermanentSubject set, PermanentPredicate filter) {
        if (filter == null) {
            return set;
        }
        return switch (set) {
            case PermanentSubject.ControlledBy controlledBy ->
                    new PermanentSubject.ControlledBy(controlledBy.players(), and(controlledBy.filter(), filter));
            case PermanentSubject.OwnedBy ownedBy ->
                    new PermanentSubject.OwnedBy(ownedBy.players(), and(ownedBy.filter(), filter));
            case PermanentSubject.AttachmentsOf attachmentsOf ->
                    new PermanentSubject.AttachmentsOf(attachmentsOf.permanents(), and(attachmentsOf.filter(), filter));
            case PermanentSubject.AttachedToPlayers attached ->
                    new PermanentSubject.AttachedToPlayers(attached.players(), and(attached.filter(), filter));
            default -> throw new IllegalArgumentException("Only a set is narrowed by a filter: " + set);
        };
    }

    private static PermanentPredicate allOfPredicates(PermanentPredicate... predicates) {
        return new PermanentAllOfPredicate(List.of(predicates));
    }

    private static PermanentPredicate and(PermanentPredicate base, PermanentPredicate filter) {
        return base instanceof PermanentTruePredicate ? filter : new PermanentAllOfPredicate(List.of(base, filter));
    }

    /** Every permanent matching {@code filter} that {@code players} own, whoever controls it. */
    public static PermanentSubject ownedBy(PlayerSubject players, PermanentPredicate filter) {
        return new PermanentSubject.OwnedBy(players, filter);
    }

    /** Every permanent matching {@code filter} attached to a permanent {@code permanents} names ("all Auras attached to it"). */
    public static PermanentSubject attachmentsOf(PermanentSubject permanents, PermanentPredicate filter) {
        return new PermanentSubject.AttachmentsOf(permanents, filter);
    }

    /** Every permanent matching {@code filter} attached to a player {@code players} names ("each Curse attached to enchanted player"). */
    public static PermanentSubject attachedToPlayers(PlayerSubject players, PermanentPredicate filter) {
        return new PermanentSubject.AttachedToPlayers(players, filter);
    }

    /** Several subjects together, each permanent once. */
    public static PermanentSubject allOf(PermanentSubject... subjects) {
        return new PermanentSubject.AllOf(List.of(subjects));
    }

    /** One of {@code options}, chosen by the controller as the effect resolves ("this permanent or target creature"). */
    public static PermanentSubject oneOf(PermanentSubject... options) {
        return new PermanentSubject.OneOf(List.of(options));
    }

    /** "You". */
    public static PlayerSubject you() {
        return YOU;
    }

    /** "Target player": the player targets the stack entry assigns to the effect. */
    public static PlayerSubject targetPlayer() {
        return TARGET_PLAYER;
    }

    /** The player targets chosen for target group {@code group}. */
    public static PlayerSubject targetPlayer(int group) {
        return new PlayerSubject.TargetPlayer(group, true);
    }

    /**
     * "Target player" read without this effect declaring a target: another effect of the same spell or ability, or
     * the card's target filter, declares it ("target player draws two cards and loses 2 life" with the life loss
     * declaring the target).
     */
    public static PlayerSubject targetPlayerDeclaredElsewhere() {
        return TARGET_PLAYER_DECLARED_ELSEWHERE;
    }

    /** "Each player". */
    public static PlayerSubject eachPlayer() {
        return EACH_PLAYER;
    }

    /** "Each opponent". */
    public static PlayerSubject eachOpponent() {
        return EACH_OPPONENT;
    }

    /** "Each of that player's opponents": every player other than those {@code player} names. */
    public static PlayerSubject opponentsOf(PlayerSubject player) {
        return new PlayerSubject.OpponentsOf(player);
    }

    /** "Its controller", "that creature's controller". */
    public static PlayerSubject controllerOf(PermanentSubject permanent) {
        return new PlayerSubject.ControllerOf(permanent);
    }

    /** "Its owner". */
    public static PlayerSubject ownerOf(PermanentSubject permanent) {
        return new PlayerSubject.OwnerOf(permanent);
    }

    /** "That player": the player the trigger event was about. */
    public static PlayerSubject triggeringPlayer() {
        return TRIGGERING_PLAYER;
    }

    /** "The active player". */
    public static PlayerSubject activePlayer() {
        return ACTIVE_PLAYER;
    }

    /** "Defending player". */
    public static PlayerSubject defendingPlayer() {
        return DEFENDING_PLAYER;
    }

    /** "Enchanted player". */
    public static PlayerSubject enchantedPlayer() {
        return ENCHANTED_PLAYER;
    }

    /** "The chosen player": the player the source chose as it entered. */
    public static PlayerSubject chosenPlayer() {
        return CHOSEN_PLAYER;
    }

    /** "The chosen players": every player the source chose as it entered. */
    public static PlayerSubject chosenPlayers() {
        return CHOSEN_PLAYERS;
    }

    /** The players who said yes to the preceding "each player may" choice of this resolution. */
    public static PlayerSubject acceptedPlayers() {
        return ACCEPTED_PLAYERS;
    }

    /** The players an earlier step of this resolution dealt damage to ("each player dealt damage this way"). */
    public static PlayerSubject playersDealtDamageThisResolution() {
        return PLAYERS_DEALT_DAMAGE;
    }

    /**
     * "That player", "each player who [drew | milled | lost life] this way", "its controller" of what a step removed: the
     * players the most recent {@code kind} step of the same resolution recorded (see {@link PlayerSubject.PlayersThisWay}).
     * A step result.
     */
    public static PlayerSubject playersThisWay(StepKind kind) {
        return new PlayerSubject.PlayersThisWay(kind);
    }

    /**
     * "Each player who discarded a nonland card this way": the owners of the cards the most recent {@code kind} step
     * recorded that match {@code cardFilter} where they are now (see {@link PlayerSubject.PlayersThisWay}).
     */
    public static PlayerSubject playersThisWay(StepKind kind, CardPredicate cardFilter) {
        return new PlayerSubject.PlayersThisWay(kind, Objects.requireNonNull(cardFilter, "cardFilter"), null);
    }

    /**
     * "Each player who sacrificed a Plains this way": the controllers of the permanents the most recent {@code kind}
     * step recorded that match {@code permanentFilter} as they are or last were on the battlefield (see
     * {@link PlayerSubject.PlayersThisWay}).
     */
    public static PlayerSubject playersThisWay(StepKind kind, PermanentPredicate permanentFilter) {
        return new PlayerSubject.PlayersThisWay(kind, null, Objects.requireNonNull(permanentFilter, "permanentFilter"));
    }

    /**
     * "Each player who received no votes": the players in the game the resolution's last vote named none of (see
     * {@link PlayerSubject.Voted}). A step result.
     */
    public static PlayerSubject playersWithNoVotes() {
        return new PlayerSubject.Voted(VoteEffect.Tally.NONE);
    }

    /** The players who won the subgame an earlier step of this resolution played. */
    public static PlayerSubject subgameWinners() {
        return SUBGAME_WINNERS;
    }

    /** The players {@code players} names but those {@code excluded} names ("the other chosen player"). */
    public static PlayerSubject except(PlayerSubject players, PlayerSubject excluded) {
        return new PlayerSubject.Except(players, excluded);
    }

    /** Every player any of {@code subjects} names, each once ("that player or that planeswalker's controller"). */
    public static PlayerSubject allOf(PlayerSubject... subjects) {
        return new PlayerSubject.AllOf(List.of(subjects));
    }

    /** The players {@code players} names who match {@code filter} ("each player who controls the most lands"). */
    public static PlayerSubject matching(PlayerSubject players, PlayerPredicate filter) {
        return new PlayerSubject.Matching(players, filter);
    }
}
