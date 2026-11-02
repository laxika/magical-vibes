package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "69")
public class AangAndKatara extends Card {

    public AangAndKatara() {
        PermanentAllOfPredicate tappedArtifactOrCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsTappedPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()))));
        PermanentCount tappedArtifactsOrCreatures = new PermanentCount(
                tappedArtifactOrCreature, CountScope.CONTROLLER);
        CreateTokenEffect allyToken = new CreateTokenEffect(
                tappedArtifactsOrCreatures, "Ally", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.ALLY), Set.of(), Set.of());

        addTrigger(Triggers.enters(ObjectRelation.THIS), allyToken);
        addTrigger(Triggers.attacks(), allyToken);
    }
}
