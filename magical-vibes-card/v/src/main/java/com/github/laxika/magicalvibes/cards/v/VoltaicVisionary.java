package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "183")
@CardRegistration(set = "DBL", collectorNumber = "450")
public class VoltaicVisionary extends Card {

    public VoltaicVisionary() {
        setBackFaceCard(new VoltChargedBerserker());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        DealDamageEffect.to(Subjects.you(), 2),
                        ExileTopCardOfOwnLibraryEffect.linkedToSource(Subjects.you()),
                        AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE))),
                "{T}: This creature deals 2 damage to you. Exile the top card of your library. You may play that card this turn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
        // When you play a card exiled with this creature, transform this creature.
        addTrigger(Triggers.youCastSpell().exiledWithThis(), new TransformSelfEffect());
        addTrigger(Triggers.youPlayLand().exiledWithThis(), new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "VoltChargedBerserker";
    }
}
