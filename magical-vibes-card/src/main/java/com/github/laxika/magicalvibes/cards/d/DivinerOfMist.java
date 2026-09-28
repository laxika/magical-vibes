package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromGraveyardAndGrantCastPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "20")
@CardRegistration(set = "TDC", collectorNumber = "60")
public class DivinerOfMist extends Card {

    public DivinerOfMist() {
        var instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        var eligibleSpell = new CardAllOfPredicate(List.of(
                instantOrSorcery,
                new CardMaxManaValuePredicate(4)));

        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new MillEffect(4, MillRecipient.CONTROLLER),
                new MayEffect(
                        new ChooseCardFromGraveyardAndGrantCastPermissionEffect(
                                eligibleSpell, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true),
                        "Cast an instant or sorcery spell with mana value 4 or less from your graveyard without paying its mana cost?")));
    }
}
