package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "28")
@CardRegistration(set = "PIP", collectorNumber = "556")
public class Vault101BirthdayParty extends Card {

    public Vault101BirthdayParty() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, SequenceEffect.of(
                new CreateTokenEffect(1, "Human Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()),
                CreateTokenEffect.ofFoodToken(1)));

        var auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardIsAuraPredicate(),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
        var putAuraOrEquipment = new SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
                auraOrEquipment, true, false, false, true);
        addEffect(EffectSlot.SAGA_CHAPTER_II, putAuraOrEquipment);
        addEffect(EffectSlot.SAGA_CHAPTER_III, putAuraOrEquipment);
    }
}
