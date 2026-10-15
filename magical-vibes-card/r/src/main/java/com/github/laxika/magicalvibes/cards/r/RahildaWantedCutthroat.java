package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesOfSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomNonlandCardFromDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "42")
public class RahildaWantedCutthroat extends Card {

    public RahildaWantedCutthroat() {
        setBackFaceCard(new RahildaFeralOutlaw());
        addAbilities();
    }

    @Override
    public String getBackFaceClassName() {
        return "RahildaFeralOutlaw";
    }

    static ConditionalEffect castExiledNonlandCards() {
        return new ConditionalEffect(
                new AnyOf(List.of(
                        new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.WOLF),
                        new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.WEREWOLF))),
                new AllowCastFromCardsExiledWithSourceEffect(
                        true,
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        false,
                        false,
                        0,
                        null,
                        false,
                        true,
                        false));
    }

    private void addAbilities() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileRandomNonlandCardFromDamagedPlayerLibraryEffect());
        addEffect(EffectSlot.STATIC, castExiledNonlandCards());
    }
}
