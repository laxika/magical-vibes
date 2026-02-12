package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.List;
import java.util.Set;

public class AvenCloudchaser extends Card {

    public AvenCloudchaser() {
        super("Aven Cloudchaser", CardType.CREATURE, "{3}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.BIRD, CardSubtype.SOLDIER));
        setCardText("Flying\nWhen Aven Cloudchaser enters the battlefield, destroy target enchantment.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(2);
        setToughness(2);
        setNeedsTarget(true);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect(Set.of(CardType.ENCHANTMENT)));
    }
}
