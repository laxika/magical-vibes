package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromGraveyardAndGrantCastPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "44")
public class MissionBriefing extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public MissionBriefing() {
        addEffect(EffectSlot.SPELL, new SurveilEffect(2));
        addEffect(EffectSlot.SPELL, new ChooseCardFromGraveyardAndGrantCastPermissionEffect(
                INSTANT_OR_SORCERY, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true));
    }
}
