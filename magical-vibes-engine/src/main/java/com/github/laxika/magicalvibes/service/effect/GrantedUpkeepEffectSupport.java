package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The upkeep-triggered abilities other permanents' static abilities grant a permanent continuously: a
 * {@code GrantEffectEffect} of a {@link CumulativeUpkeepEffect} (Breath of Dreams: green creatures have cumulative
 * upkeep), which is the ability "at the beginning of your upkeep, …" (CR 702.24a), and a
 * {@link GrantTriggeredAbilityEffect} of an {@code Triggers.upkeep(ActivePlayer.YOU)} ability (Avarice Amulet's "at
 * the beginning of your upkeep, draw a card"). {@link #grantedEachUpkeepAbilities} gives the
 * {@code Triggers.upkeep(ActivePlayer.ANY)} grants (Verdant Embrace), controlled by the granted permanent's controller.
 * The step reader judges their options.
 */
@Component
@RequiredArgsConstructor
public class GrantedUpkeepEffectSupport {

    private static final StepTrigger YOUR_UPKEEP = Triggers.upkeep(ActivePlayer.YOU);
    private static final StepTrigger EACH_UPKEEP = Triggers.upkeep(ActivePlayer.ANY);

    private final GameQueryService gameQueryService;

    /** The "at the beginning of your upkeep" abilities granted to {@code permanent}, in the order granted. */
    public List<TriggeredAbility> grantedUpkeepAbilities(GameData gameData, Permanent permanent) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (CardEffect granted : gameQueryService.computeStaticBonus(gameData, permanent).grantedEffects()) {
            if (granted instanceof CumulativeUpkeepEffect) {
                abilities.add(new TriggeredAbility(YOUR_UPKEEP, granted));
            } else if (granted instanceof GrantTriggeredAbilityEffect grant
                    && grant.grantedAbility() instanceof TriggeredAbility ability
                    && ability.hasTrigger(YOUR_UPKEEP)) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    /** The "at the beginning of each upkeep" abilities granted to {@code permanent}, in the order granted. */
    public List<TriggeredAbility> grantedEachUpkeepAbilities(GameData gameData, Permanent permanent) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (CardEffect granted : gameQueryService.computeStaticBonus(gameData, permanent).grantedEffects()) {
            if (granted instanceof GrantTriggeredAbilityEffect grant
                    && grant.grantedAbility() instanceof TriggeredAbility ability
                    && ability.hasTrigger(EACH_UPKEEP)) {
                abilities.add(ability);
            }
        }
        return abilities;
    }
}
