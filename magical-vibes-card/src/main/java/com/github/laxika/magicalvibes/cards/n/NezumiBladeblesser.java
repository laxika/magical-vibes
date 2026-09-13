package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "NEO", collectorNumber = "115")
public class NezumiBladeblesser extends Card {

    public NezumiBladeblesser() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentIsArtifactPredicate()),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentIsEnchantmentPredicate()),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)));
    }
}
