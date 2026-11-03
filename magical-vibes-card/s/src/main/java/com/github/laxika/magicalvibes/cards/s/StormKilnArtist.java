package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "115")
@CardRegistration(set = "SOC", collectorNumber = "255")
@CardRegistration(set = "CMM", collectorNumber = "260")
@CardRegistration(set = "CMM", collectorNumber = "644")
@CardRegistration(set = "SLD", collectorNumber = "2352")
@CardRegistration(set = "TDC", collectorNumber = "235")
@CardRegistration(set = "OTC", collectorNumber = "181")
public class StormKilnArtist extends Card {

    public StormKilnArtist() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new BoostSelfEffect(artifactsYouControl, new Fixed(0)));

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> magecraft = List.of(CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), magecraft.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell(),
                new SpellCopyTriggerEffect(instantOrSorcery, magecraft));
    }
}
