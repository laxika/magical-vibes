package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMKM", collectorNumber = "17")
public class GuildpactGreenwalker extends Card {

    public GuildpactGreenwalker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect.incorporatingAndGetting(
                        new CardTypePredicate(CardType.CREATURE), "{1}{G}", 4, 4),
                "Choose a creature card in your hand to perpetually incorporate {1}{G} and get +4/+4?"));

        var multicoloredCreature = new PermanentIsMulticoloredPredicate();
        addStaticEffect(new GrantKeywordEffect(
                Keyword.WARD, Subjects.otherCreaturesYouControl(), multicoloredCreature));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(2)), Subjects.otherCreaturesYouControl(), multicoloredCreature));
    }
}
