package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ModalSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "227")
public class RikuOfManyPaths extends Card {

    public RikuOfManyPaths() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new ModalSpellCastTriggerEffect(List.of(
                new ChooseOneOption("Exile the top card of your library. Until the end of your next turn, you may play it",
                        new ExileTopCardsMayPlayUntilNextTurnEffect(1)),
                new ChooseOneOption("Put a +1/+1 counter on Riku of Many Paths. It gains trample until end of turn",
                        List.of(new PutCountersOnSourceEffect(1, 1, 1),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF))),
                new ChooseOneOption("Create a 1/1 blue Bird creature token with flying",
                        new CreateTokenEffect(1, "Bird", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()))
        )));
    }
}
