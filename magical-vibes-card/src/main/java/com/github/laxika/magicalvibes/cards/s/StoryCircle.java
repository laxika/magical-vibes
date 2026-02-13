package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;

public class StoryCircle extends Card {

    public StoryCircle() {
        super("Story Circle", CardType.ENCHANTMENT, "{1}{W}{W}", CardColor.WHITE);

        setCardText("As Story Circle enters the battlefield, choose a color.\n{W}: The next time a source of the chosen color would deal damage to you this turn, prevent that damage.");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.MANA_ACTIVATED_ABILITY, new PreventNextColorDamageToControllerEffect());
        setManaActivatedAbilityCost("{W}");
    }
}
