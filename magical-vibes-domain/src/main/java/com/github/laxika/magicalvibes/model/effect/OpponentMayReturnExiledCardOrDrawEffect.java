package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Used in the may-ability system when the controller's opponents are asked, one at a time in turn order, whether to
 * let the controller have an exiled card ("any opponent may", Distant Memories). The first who does ends the asking;
 * if every opponent declines, the controller draws {@code drawCount} cards instead.
 *
 * @param drawCount    number of cards the controller draws if every opponent declines
 * @param controllerId the player whose exiled card it is and who draws
 */
public record OpponentMayReturnExiledCardOrDrawEffect(int drawCount, UUID controllerId) implements CardEffect {
}
