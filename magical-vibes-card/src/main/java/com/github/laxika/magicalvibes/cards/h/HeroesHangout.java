package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "79")
public class HeroesHangout extends Card {

    public HeroesHangout() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Choose one of them. Until the end of your next turn, you may play that card",
                        new ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "One or two target creatures each get +1/+0 and gain first strike until end of turn",
                        List.<CardEffect>of(
                                new BoostTargetCreatureEffect(1, 0),
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                        TargetFilters.creature(), null, 1, 2, false, null)
        )));
    }
}
