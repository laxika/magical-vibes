package com.github.laxika.magicalvibes.service.combat.block;

import com.github.laxika.magicalvibes.model.Permanent;
import org.springframework.stereotype.Component;

/**
 * Renders a {@link BlockDenial} as the sentence shown to the player who tried to declare the block.
 * Presentation only: it decides no legality, reads no game state, and holds nothing — the rules
 * question is answered entirely by {@link BlockLegalityService}, which hands over the finished
 * verdict and the two creatures to name.
 *
 * <p>Every message here is the wording the block-legality checks have always surfaced. Treat them as
 * pinned: {@code BlockLegalityContextTest} asserts the message a given board produces, so a reworded
 * arm is a test change, not a silent one.
 */
@Component
public class BlockDenialMessageService {

    /** Describes why {@code blocker} may not block {@code attacker}. */
    public String describe(BlockDenial denial, Permanent blocker, Permanent attacker) {
        String blockerName = blocker.getCard().getName();
        String attackerName = attacker.getCard().getName();
        return switch (denial.reason()) {
            case CANT_BE_BLOCKED -> attackerName + " can't be blocked";
            case FLYING -> blockerName + " cannot block " + attackerName + " (flying)";
            case HORSEMANSHIP -> blockerName + " cannot block " + attackerName + " (horsemanship)";
            case FEAR -> blockerName + " cannot block " + attackerName + " (fear)";
            case INTIMIDATE -> blockerName + " cannot block " + attackerName + " (intimidate)";
            case SKULK -> blockerName + " cannot block " + attackerName + " (skulk)";
            case BLOCKER_LIMITED_TO_ATTACKERS -> blockerName + " can only block " + denial.detail();
            case GLOBAL_RESTRICTION -> denial.detail();
            case ATTACKER_LIMITED_TO_BLOCKERS -> attackerName + " can only be blocked by " + denial.detail();
            case CANT_BE_BLOCKED_BY_MATCHING -> blockerName + " cannot block " + attackerName;
            case CANT_BE_BLOCKED_BY_LESS_POWER ->
                    blockerName + " cannot block " + attackerName + " (power too low)";
            case LANDWALK -> attackerName + " can't be blocked (" + denial.detail() + "walk)";
            case CANT_BLOCK_THIS_TURN -> blockerName + " can't block this turn";
            case CANT_BLOCK -> blockerName + " can't block";
            case CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS ->
                    blockerName + " can't block " + attackerName + " (power too high)";
            case CANT_BLOCK_HIGH_POWER ->
                    blockerName + " can't block " + attackerName + " (power too high)";
            case CANT_BLOCK_THAT_ATTACKER -> blockerName + " can't block " + attackerName + " this turn";
            case PROTECTION -> blockerName + " cannot block " + attackerName + " (protection)";
        };
    }
}
