package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player may bid life for control of target creature. The controller starts the bidding at 0;
 * then, in turn order, each player may top the high bid. The bidding ends once the high bid stands
 * (comes back around to the high bidder with no raise). The high bidder loses life equal to the high
 * bid (a life loss, so a player may bid more life than they have) and gains control of the creature
 * indefinitely ({@code ControlDuration.PERMANENT}).
 * <p>
 * Used by Illicit Auction. Requires repeated player interaction (a life-bid auction); the target
 * creature travels on the stack entry, progress lives on {@code GameData.illicitAuction}.
 */
public record IllicitAuctionEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
