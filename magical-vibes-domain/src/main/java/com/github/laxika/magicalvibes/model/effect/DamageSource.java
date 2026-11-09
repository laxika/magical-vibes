package com.github.laxika.magicalvibes.model.effect;

/**
 * Which object deals a damage effect's damage. What damage does depends on its source's characteristics (CR 120.3):
 * the source's lifelink, infect and wither, the colors and types protection and prevention read, and the source a
 * "whenever a [type] deals damage" trigger sees.
 */
public enum DamageSource {
    /** The spell or ability's own source ("this creature deals", "Shock deals"). */
    ABILITY_SOURCE,
    /**
     * The permanent that dealt the damage the trigger event was about ("that Archer deals that much damage to that
     * creature's controller", "whenever a creature you control deals combat damage to a player, it deals that much
     * damage to …"), as it is now or as it last existed on the battlefield (CR 608.2h: "if an ability states that an
     * object does something, it's the object … that does it, not the ability"). Read from
     * {@code StackEntry.getEventDamageSource()}, which the damage collectors record; an ability reaching the stack
     * without it fails as it resolves.
     */
    EVENT_DAMAGE_SOURCE,
    /**
     * The permanent the trigger event was about ("then that land deals 1 damage to that player", Barbflare Gremlin), as
     * it is now or as it last existed on the battlefield (CR 608.2h). Read from {@code StackEntry.getTriggeringPermanentId()}
     * and, once it has left, {@code StackEntry.getTriggeringPermanentSnapshot()}; with neither, no damage is dealt.
     */
    TRIGGERING_PERMANENT
}
