package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "164")
@CardRegistration(set = "ELD", collectorNumber = "395")
@CardRegistration(set = "AFC", collectorNumber = "162")
@CardRegistration(set = "WOC", collectorNumber = "128")
@CardRegistration(set = "FDC", collectorNumber = "212")
public class KenrithsTransformation extends Card {

    public KenrithsTransformation() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1))
                .addStaticEffect(new SetCardTypesEffect(Set.of(CardType.CREATURE), GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.ELK, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new GrantColorEffect(CardColor.GREEN, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new SetBasePowerToughnessEffect(3, 3, GrantScope.ENCHANTED_CREATURE));
    }
}
