package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;

import java.util.List;

public class StoryCircle extends Card {

    public StoryCircle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(new PreventNextColorDamageToControllerEffect()), false, "{W}: The next time a source of the chosen color would deal damage to you this turn, prevent that damage."));
    }
}
