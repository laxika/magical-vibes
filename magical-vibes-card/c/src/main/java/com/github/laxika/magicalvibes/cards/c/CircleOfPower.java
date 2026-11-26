package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "92")
@CardRegistration(set = "FIN", collectorNumber = "583")
public class CircleOfPower extends Card {

    public CircleOfPower() {
        PermanentHasSubtypePredicate wizard = new PermanentHasSubtypePredicate(CardSubtype.WIZARD);
        List<Ability> tokenEffects = List.of(
                new TriggeredAbility(Triggers.youCastSpell()
                                .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                        DealDamageEffect.to(Subjects.eachOpponent(), 1)));
        CreateTokenEffect wizardToken = new CreateTokenEffect(
                1, "Wizard", 0, 1, CardColor.BLACK, List.of(CardSubtype.WIZARD),
                Set.of(), Set.of(), tokenEffects);

        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LoseLifeEffect(2));
        addSpellEffect(wizardToken);
        addSpellEffect(BoostEffect.creaturesYouControl(1, 0, wizard));
        addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.otherCreaturesYouControl(), wizard));
    }
}
