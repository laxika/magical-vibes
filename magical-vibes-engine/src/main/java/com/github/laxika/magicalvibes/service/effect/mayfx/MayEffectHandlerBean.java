package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * A self-contained, Spring-managed handler for a single "you may …" ability whose accept/decline
 * flow used to live as a bespoke effect-type branch inside
 * {@code MayAbilityHandlerService.handleMayAbilityChosen}.
 *
 * <p>Each migrated may-ability lives in its own {@code @Component} implementing this interface
 * (the package is {@code mayfx}, mirroring the {@code normalfx}/{@code staticfx} naming). The handler
 * declares which {@link CardEffect} type it handles via {@link #handledEffect()};
 * {@code MayEffectHandlerRegistry} dispatches by {@code effect.getClass()}.
 *
 * <p>{@link #handle} receives the same arguments the old branch had: the game state, the responding
 * player, whether they accepted, and the pending ability being resolved. Handlers that need a typed
 * view of their effect extract it from {@code ability.effects()} themselves (mirroring the old
 * {@code filter/map/findFirst} the branch used).
 */
public interface MayEffectHandlerBean {

    Class<? extends CardEffect> handledEffect();

    void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability);
}
