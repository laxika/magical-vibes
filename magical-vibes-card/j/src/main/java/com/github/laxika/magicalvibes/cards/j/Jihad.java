package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@CardRegistration(set = "ARN", collectorNumber = "5")
public class Jihad extends Card {

    private static final PermanentAllOfPredicate NONTOKEN_PERMANENT_OF_CHOSEN_COLOR =
            new PermanentAllOfPredicate(List.of(
                    new PermanentHasSourceChosenColorPredicate(),
                    new PermanentNotPredicate(new PermanentIsTokenPredicate())));

    public Jihad() {
        // As this enchantment enters, choose a color and an opponent.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentOnEnterEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());

        // White creatures get +2/+1 while the chosen player controls a matching nontoken permanent.
        // In the two-player engine, the chosen player is the single opponent.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsPermanent(NONTOKEN_PERMANENT_OF_CHOSEN_COLOR),
                new StaticBoostEffect(2, 1, GrantScope.ALL_CREATURES,
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)))));

        // When the chosen player controls no matching nontoken permanent, sacrifice Jihad.
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) ->
                        !controlsMatchingChosenColorPermanent(gameData, sourcePermanent, controllerId),
                List.of(new SacrificeSelfEffect()),
                "Jihad's state-triggered ability"
        ));
    }

    private static boolean controlsMatchingChosenColorPermanent(GameData gameData,
                                                                 Permanent sourcePermanent,
                                                                 UUID controllerId) {
        if (sourcePermanent == null || sourcePermanent.getChosenColor() == null) {
            return false;
        }
        UUID chosenPlayerId = sourcePermanent.getRememberedTargetPlayerId();
        if (chosenPlayerId == null) {
            chosenPlayerId = gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(controllerId))
                    .findFirst()
                    .orElse(null);
        }
        if (chosenPlayerId == null) {
            return false;
        }
        CardColor chosenColor = sourcePermanent.getChosenColor();
        return gameData.playerBattlefields.getOrDefault(chosenPlayerId, List.of()).stream()
                .anyMatch(permanent -> !permanent.getCard().isToken()
                        && permanent.getEffectiveColors().contains(chosenColor));
    }
}
