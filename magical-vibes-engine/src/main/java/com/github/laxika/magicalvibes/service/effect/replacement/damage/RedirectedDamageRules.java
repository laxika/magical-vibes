package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import java.util.Set;

/**
 * Which damage effects already applied to an event stay spent once part of its damage is
 * redirected to another player or permanent (CR 614.5). An effect of the damage's source or of the
 * whole game (Furnace of Rath, a source's "deals that much damage plus 1") is the same effect
 * whoever is dealt the damage, so it can't apply again; that includes a redirect of the source's
 * own damage (Goblin Psychopath, Opal-Eye) that sent it there. An effect of the original recipient (its
 * prevention shields, "damage dealt to this creature is doubled") is not an effect of the new
 * recipient, so the new recipient's own effects of that family still apply.
 */
public final class RedirectedDamageRules {

    private static final Set<String> SOURCE_SIDE_FAMILIES = Set.of(
            "GlobalDamageMultiplier", "SourceMultiplier", "SourceBonus", "NoncreatureSourceBonus",
            "PlayersAndBattlesBonus", "ToOpponentsBonus", "ControllerToOpponentBonus", "SpellToOpponentsBonus",
            "ControllerNoncombatBonus", "PerpetualNoncombatBonus", "SpellDamageReduction", "OjerAxonil",
            "ReflectDamage", "TargetSorceryRedirect", "SourceNextCombatDamageToController",
            "SourceNextCombatDamageToOpponentRedirect", "SourceNextRedirectToPermanent",
            // A redirect shield that already moved this damage can't move it again (CR 614.5).
            "TurnRedirectToCreature");

    private RedirectedDamageRules() {
    }

    /** Whether the applied effect with option key {@code key} stays spent for the redirected damage. */
    public static boolean staysSpent(String key) {
        int end = key.length();
        for (char separator : new char[] {':', '@'}) {
            int index = key.indexOf(separator);
            if (index >= 0 && index < end) {
                end = index;
            }
        }
        return SOURCE_SIDE_FAMILIES.contains(key.substring(0, end));
    }
}
