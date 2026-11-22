package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLZ", collectorNumber = "58")
@CardRegistration(set = "SLZ", collectorNumber = "179")
@CardRegistration(set = "SLZ", collectorNumber = "300")
public class GlimpseTheImpossible extends Card {

    private static final CreateTokenEffect ELDRAZI_SPAWN_TOKEN = new CreateTokenEffect(
            CardType.CREATURE,
            1,
            "Eldrazi Spawn",
            0,
            1,
            null,
            null,
            List.of(CardSubtype.ELDRAZI, CardSubtype.SPAWN),
            Set.of(),
            Set.of(),
            false,
            false,
            List.of(),
            List.of(new ActivatedAbility(
                    false,
                    null,
                    List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.COLORLESS)),
                    "Sacrifice this token: Add {C}."
            )),
            false,
            false,
            false,
            0,
            Set.of());

    public GlimpseTheImpossible() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(3, false));
        addSpellEffect(RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.EXILE)),
                        ELDRAZI_SPAWN_TOKEN.withAmount(new AmountThisWay(StepKind.PUT_INTO_GRAVEYARD)))
                .onlyIfAnyStillExiled());
    }
}
