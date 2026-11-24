package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Static replacement effect (CR 614.1a): "if a [quality] source [you control] would deal [noncombat] damage to
 * [recipients], it deals that much damage plus N instead". Applied once to a damage event (CR 614.5); the damaged
 * player, or the damaged permanent's controller, orders it among the other replacement effects (CR 616.1). Every
 * "plus N" static ability and floated "this turn" bonus is this record, read by
 * {@code GameQueryService.getAdditionalDamageBonuses}: a static ability of a permanent that lost its abilities or is
 * face down doesn't apply (CR 613.1f, 708.2a), and the source is judged as it deals damage (CR 609.7b), its colours
 * those it has for damage (Ghostly Flame) and a source that left the battlefield by its last known information
 * (CR 113.7a). A floated copy ({@link GrantStaticEffectToPlayerEffect}, "this turn") is the static ability of the
 * player it is floated on, who is its "you".
 *
 * <p>Defaults: sources you control, any of them, combat and noncombat damage, to your opponents and the permanents
 * they control (Far Fortune, Fated Firepower).
 *
 * @param amount            N, evaluated against the permanent carrying the effect (Fated Firepower's fire counters,
 *                          Hawkeye's power)
 * @param noncombatOnly     "noncombat damage" (Torture Pit, Artist's Talent, Hawkeye)
 * @param excludesSource    "another source": the permanent carrying the effect is no such source (Thor, Embermaw
 *                          Hellion)
 * @param sourceControllers whose sources: {@link Subjects#you()} ("a source you control") or
 *                          {@link Subjects#eachPlayer()} ("a source", "a spell": Rem Karolus, Tok-Tok)
 * @param sourceFilter      what the source must match, or {@code null} for any source: judged on a permanent source,
 *                          else on the spell's or other source's card; a colour leaf reads the colours it has for
 *                          damage ("a red or artifact source": Mechanized Warfare; "a noncreature source": Disciples of
 *                          the Inferno; "a Lizard, Mouse, Otter, or Raccoon": Valley Flamecaller)
 * @param spellsOnly        "a spell": only damage a spell on the stack deals itself, not damage a spell has another
 *                          object deal, such as a fight (Rem Karolus's ruling, Sulfuric Vapors)
 * @param players           the damaged players: {@link Subjects#eachOpponent()}, {@link Subjects#eachPlayer()}, or
 *                          {@code null} for none
 * @param permanents        the damaged permanents: {@code controlledBy(eachOpponent() | eachPlayer(), filter)}, or
 *                          {@code null} for none ("a creature, battle, or opponent", "a player or battle")
 */
public record AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(
        DynamicAmount amount, boolean noncombatOnly, boolean excludesSource, PlayerSubject sourceControllers,
        PermanentPredicate sourceFilter, boolean spellsOnly, PlayerSubject players, PermanentSubject permanents)
        implements CardEffect {

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(sourceControllers, "sourceControllers");
        if (!sourceControllers.equals(Subjects.you()) && !sourceControllers.equals(Subjects.eachPlayer())) {
            throw new IllegalArgumentException("Sources you control or any source: " + sourceControllers);
        }
        if (players != null && !isEachOpponentOrPlayer(players)) {
            throw new IllegalArgumentException("Damage to each opponent, each player or no player: " + players);
        }
        if (permanents != null && !(permanents instanceof PermanentSubject.ControlledBy their
                && isEachOpponentOrPlayer(their.players()))) {
            throw new IllegalArgumentException("Damage to permanents opponents or players control, or none: "
                    + permanents);
        }
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(DynamicAmount amount, boolean noncombatOnly,
                                                                         boolean excludesSource) {
        this(amount, noncombatOnly, excludesSource, Subjects.you(), null, false, Subjects.eachOpponent(),
                Subjects.controlledBy(Subjects.eachOpponent()));
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(int amount) {
        this(new Fixed(amount), false, false);
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(int amount, boolean noncombatOnly) {
        this(new Fixed(amount), noncombatOnly, false);
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(
            int amount, boolean noncombatOnly, boolean excludesSource) {
        this(new Fixed(amount), noncombatOnly, excludesSource);
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(DynamicAmount amount) {
        this(amount, false, false);
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(
            DynamicAmount amount, boolean noncombatOnly) {
        this(amount, noncombatOnly, false);
    }

    /** "If another [quality] source you control": not the permanent carrying this effect. */
    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect anotherSource() {
        return new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(amount, noncombatOnly, true,
                sourceControllers, sourceFilter, spellsOnly, players, permanents);
    }

    /** "If a [quality] source": only sources matching {@code filter}. */
    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect fromSources(PermanentPredicate filter) {
        return new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(amount, noncombatOnly,
                excludesSource, sourceControllers, filter, spellsOnly, players, permanents);
    }

    /** "If a source would deal damage", whoever controls it. */
    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect fromAnyonesSources() {
        return new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(amount, noncombatOnly,
                excludesSource, Subjects.eachPlayer(), sourceFilter, spellsOnly, players, permanents);
    }

    /** "If a spell would deal damage": only damage spells deal themselves. */
    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect fromSpells() {
        return new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(amount, noncombatOnly,
                excludesSource, sourceControllers, sourceFilter, true, players, permanents);
    }

    /** "... would deal damage to {@code damagedPlayers} or {@code damagedPermanents}" (either may be {@code null}). */
    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect to(PlayerSubject damagedPlayers,
                                                                            PermanentSubject damagedPermanents) {
        return new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(amount, noncombatOnly,
                excludesSource, sourceControllers, sourceFilter, spellsOnly, damagedPlayers, damagedPermanents);
    }

    /** "... would deal damage to an opponent": no permanent. */
    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect toOpponentsOnly() {
        return to(Subjects.eachOpponent(), null);
    }

    /** "... would deal damage to a permanent or player". */
    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect toAnyPermanentOrPlayer() {
        return to(Subjects.eachPlayer(), Subjects.controlledBy(Subjects.eachPlayer()));
    }

    private static boolean isEachOpponentOrPlayer(PlayerSubject players) {
        return players.equals(Subjects.eachOpponent()) || players.equals(Subjects.eachPlayer());
    }
}
